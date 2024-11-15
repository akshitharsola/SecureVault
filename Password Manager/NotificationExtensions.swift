import Foundation

// Consolidate all app-wide notifications in one place
extension Notification.Name {
    // Theme notifications
    static let themeDidChange = Notification.Name("com.passwordmanager.themeDidChange")
    
    // Authentication notifications - only if needed, removing if causing conflicts
    // static let authenticationDidSucceed = Notification.Name("com.passwordmanager.authenticationDidSucceed")
    // static let authenticationDidFail = Notification.Name("com.passwordmanager.authenticationDidFail")
}
