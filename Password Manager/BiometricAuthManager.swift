import LocalAuthentication

class BiometricAuthManager {
    static let shared = BiometricAuthManager()
    
    private let context = LAContext()
    private var authError: NSError?
    
    // Authentication state management
    private var lastAuthenticationTime: Date?
    private let authenticationTimeout: TimeInterval = 60 // 1 minute timeout
    private var isCurrentlyAuthenticated = false
    
    private init() {}
    
    var biometricType: String {
        let _ = context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &authError)
        switch context.biometryType {
        case .faceID:
            return "Face ID"
        case .touchID:
            return "Touch ID"
        case .none:
            return "Passcode"
        case .opticID:
            return "Optical ID"
        @unknown default:
            return "Authentication"
        }
    }
    
    var isAuthenticated: Bool {
        guard let lastAuth = lastAuthenticationTime else { return false }
        return isCurrentlyAuthenticated && Date().timeIntervalSince(lastAuth) < authenticationTimeout
    }
    
    func canUseBiometrics() -> (Bool, String) {
        var error: NSError?
        let canEvaluate = context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
        
        var message = ""
        if let error = error as? LAError {
            switch error.code {
            case .biometryNotEnrolled:
                message = "\(biometricType) is not set up on this device."
            case .biometryNotAvailable:
                message = "\(biometricType) is not available on this device."
            case .biometryLockout:
                message = "\(biometricType) is locked due to too many failed attempts. Please use your device passcode."
            case .passcodeNotSet:
                message = "Please set up a device passcode to use \(biometricType)."
            default:
                message = "\(biometricType) may not be configured correctly."
            }
        }
        
        return (canEvaluate, message)
    }
    
    func authenticateUser(reason: String, completion: @escaping (Result<Void, AuthError>) -> Void) {
        // Check if already authenticated within timeout period
        if isAuthenticated {
            completion(.success(()))
            return
        }
        
        // First try biometric authentication
        let (canUseBiometrics, _) = canUseBiometrics()
        
        // Reset authentication state
        isCurrentlyAuthenticated = false
        lastAuthenticationTime = nil
        
        // If biometrics are available, try that first
        if canUseBiometrics {
            context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: reason) { [weak self] success, error in
                DispatchQueue.main.async {
                    if success {
                        self?.setAuthenticated()
                        completion(.success(()))
                    } else if let error = error as? LAError {
                        switch error.code {
                        case .biometryLockout, .biometryNotAvailable:
                            self?.authenticateWithPasscode(reason: reason, completion: completion)
                        case .userFallback:
                            self?.authenticateWithPasscode(reason: reason, completion: completion)
                        case .userCancel:
                            completion(.failure(.biometricError(error)))
                        default:
                            completion(.failure(.biometricError(error)))
                        }
                    } else {
                        completion(.failure(.unknown))
                    }
                }
            }
        } else {
            // If biometrics aren't available, try passcode
            authenticateWithPasscode(reason: reason, completion: completion)
        }
    }
    
    private func authenticateWithPasscode(reason: String, completion: @escaping (Result<Void, AuthError>) -> Void) {
        context.evaluatePolicy(.deviceOwnerAuthentication, localizedReason: reason) { [weak self] success, error in
            DispatchQueue.main.async {
                if success {
                    self?.setAuthenticated()
                    completion(.success(()))
                } else if let error = error as? LAError {
                    completion(.failure(.passcodeError(error)))
                } else {
                    completion(.failure(.unknown))
                }
            }
        }
    }
    
    func invalidateAuthentication() {
        isCurrentlyAuthenticated = false
        lastAuthenticationTime = nil
    }
    
    private func setAuthenticated() {
        isCurrentlyAuthenticated = true
        lastAuthenticationTime = Date()
        
        // Auto-invalidate after timeout
        DispatchQueue.main.asyncAfter(deadline: .now() + authenticationTimeout) { [weak self] in
            self?.invalidateAuthentication()
        }
    }
    
    // Custom error enum for better error handling
    enum AuthError: LocalizedError {
        case biometricError(LAError)
        case passcodeError(LAError)
        case unknown
        
        var errorDescription: String? {
            switch self {
            case .biometricError(let error):
                switch error.code {
                case .biometryNotEnrolled:
                    return "\(BiometricAuthManager.shared.biometricType) is not set up. Please check your device settings."
                case .biometryNotAvailable:
                    return "\(BiometricAuthManager.shared.biometricType) is not available on this device."
                case .biometryLockout:
                    return "\(BiometricAuthManager.shared.biometricType) is locked. Please use your device passcode."
                case .userCancel:
                    return "Authentication was canceled."
                case .userFallback:
                    return "Using device passcode instead."
                case .systemCancel:
                    return "Authentication was canceled by the system."
                case .passcodeNotSet:
                    return "Please set up a device passcode in Settings."
                case .invalidContext:
                    return "Authentication is not available at this time."
                default:
                    return error.localizedDescription
                }
            case .passcodeError(let error):
                switch error.code {
                case .passcodeNotSet:
                    return "No passcode is set on this device. Please set a passcode in your device settings."
                case .userCancel:
                    return "Authentication was canceled."
                case .invalidContext:
                    return "Authentication is not available at this time."
                default:
                    return "Passcode authentication failed."
                }
            case .unknown:
                return "An unknown error occurred during authentication."
            }
        }
    }
}

// MARK: - Notification Names
extension Notification.Name {
    static let authenticationDidSucceed = Notification.Name("authenticationDidSucceed")
    static let authenticationDidFail = Notification.Name("authenticationDidFail")
}
