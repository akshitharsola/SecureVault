import UIKit
import LocalAuthentication

class PasswordDetailViewController: UIViewController {
    // MARK: - Properties
    private var password: Password
    private var isPasswordVisible = false {
        didSet {
            updatePasswordVisibility()
        }
    }
    
    private let notesLabel: UILabel = {
            let label = UILabel()
            label.font = .systemFont(ofSize: 14, weight: .medium)
            label.text = "Notes"
            label.translatesAutoresizingMaskIntoConstraints = false
            return label
        }()
        
        private let notesTextView: UITextView = {
            let textView = UITextView()
            textView.font = .systemFont(ofSize: 16)
            textView.isEditable = false
            textView.layer.borderColor = UIColor.systemGray4.cgColor
            textView.layer.borderWidth = 1
            textView.layer.cornerRadius = 8
            textView.translatesAutoresizingMaskIntoConstraints = false
            textView.backgroundColor = .systemBackground
            return textView
        }()
    
    // Track authentication state
    private var lastAuthTime: Date?
    private let authTimeoutInterval: TimeInterval = 60 // 1 minute timeout
    
    private func createDetailSection(title: String, value: String, showCopy: Bool) -> UIView {
            let container = UIView()
            container.translatesAutoresizingMaskIntoConstraints = false
            
            let titleLabel = UILabel()
            titleLabel.font = .systemFont(ofSize: 14, weight: .medium)
            titleLabel.text = title
            titleLabel.translatesAutoresizingMaskIntoConstraints = false
            
            let valueLabel = UILabel()
            valueLabel.font = .systemFont(ofSize: 16)
            valueLabel.text = value
            valueLabel.translatesAutoresizingMaskIntoConstraints = false
            
            container.addSubview(titleLabel)
            container.addSubview(valueLabel)
            
            if showCopy {
                let copyButton = UIButton(type: .system)
                copyButton.setImage(UIImage(systemName: "doc.on.doc"), for: .normal)
                copyButton.addTarget(self, action: #selector(copyValue(_:)), for: .touchUpInside)
                copyButton.translatesAutoresizingMaskIntoConstraints = false
                container.addSubview(copyButton)
                
                NSLayoutConstraint.activate([
                    copyButton.centerYAnchor.constraint(equalTo: valueLabel.centerYAnchor),
                    copyButton.trailingAnchor.constraint(equalTo: container.trailingAnchor),
                    copyButton.widthAnchor.constraint(equalToConstant: 44),
                    copyButton.heightAnchor.constraint(equalToConstant: 44)
                ])
                
                valueLabel.trailingAnchor.constraint(equalTo: copyButton.leadingAnchor, constant: -8).isActive = true
            } else {
                valueLabel.trailingAnchor.constraint(equalTo: container.trailingAnchor).isActive = true
            }
            
            NSLayoutConstraint.activate([
                titleLabel.topAnchor.constraint(equalTo: container.topAnchor),
                titleLabel.leadingAnchor.constraint(equalTo: container.leadingAnchor),
                titleLabel.trailingAnchor.constraint(equalTo: container.trailingAnchor),
                
                valueLabel.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 4),
                valueLabel.leadingAnchor.constraint(equalTo: container.leadingAnchor),
                valueLabel.bottomAnchor.constraint(equalTo: container.bottomAnchor)
            ])
            
            return container
        }

    
    // MARK: - UI Components
    private let stackView: UIStackView = {
        let stack = UIStackView()
        stack.axis = .vertical
        stack.spacing = 16
        stack.translatesAutoresizingMaskIntoConstraints = false
        return stack
    }()
    
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 20, weight: .bold)
        return label
    }()
    
    private var passwordValueLabel: UILabel?
    private var passwordToggleButton: UIButton?
    
    // MARK: - Initialization
    init(password: Password) {
        self.password = password
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: - Lifecycle
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupNotifications()
        applyTheme(ThemeManager.loadTheme())
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        // Always hide password when leaving the view
        if isPasswordVisible {
            isPasswordVisible = false
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    // MARK: - Setup
    private func setupNotifications() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(themeDidChange),
            name: .themeDidChange,
            object: nil
        )
    }
    
    private func setupUI() {
        view.backgroundColor = ThemeManager.loadTheme().backgroundColor
        
        view.addSubview(stackView)
        stackView.addArrangedSubview(createCopySection())
        stackView.addArrangedSubview(notesLabel)
        stackView.addArrangedSubview(notesTextView)
        // Add sections to stack view
        stackView.addArrangedSubview(titleLabel)
        stackView.addArrangedSubview(createUsernameSection())
        stackView.addArrangedSubview(createPasswordSection())
        
        titleLabel.text = password.title
        let notesLabel = UILabel()
        notesLabel.font = .systemFont(ofSize: 14, weight: .medium)
        notesLabel.text = "Notes"
        notesLabel.translatesAutoresizingMaskIntoConstraints = false
        let notesTextView = UITextView()
        notesTextView.font = .systemFont(ofSize: 16)
        notesTextView.isEditable = false
        notesTextView.layer.borderColor = UIColor.systemGray4.cgColor
        notesTextView.layer.borderWidth = 1
        notesTextView.layer.cornerRadius = 8
        notesTextView.translatesAutoresizingMaskIntoConstraints = false
        notesTextView.backgroundColor = .systemBackground
        notesTextView.text = password.notes ?? "No notes"
        stackView.addArrangedSubview(notesLabel)
        stackView.addArrangedSubview(notesTextView)
        
        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 16),
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            stackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
            notesTextView.heightAnchor.constraint(greaterThanOrEqualToConstant: 100)
        ])
//        notesTextView.text = password.notes ?? "No notes"
                
        setupNavigationBar()
        applyTheme(ThemeManager.loadTheme())
    }
    
    private func setupNavigationBar() {
            let editButton = UIBarButtonItem(
                barButtonSystemItem: .edit,
                target: self,
                action: #selector(editTapped)
            )
            
            let deleteButton = UIBarButtonItem(
                image: UIImage(systemName: "trash"),
                style: .plain,
                target: self,
                action: #selector(deleteTapped)
            )
            deleteButton.tintColor = .systemRed
            
            navigationItem.rightBarButtonItems = [editButton, deleteButton]
        }
    
    @objc private func editTapped() {
        let formVC = PasswordFormViewController()
        formVC.passwordToEdit = password
        formVC.onSave = { [weak self] updatedPassword in
            self?.password = updatedPassword
            PasswordManager.shared.updatePassword(updatedPassword)
            self?.setupUI()
        }
        let navController = UINavigationController(rootViewController: formVC)
        present(navController, animated: true)
    }
    
    @objc private func deleteTapped() {
            let alert = UIAlertController(
                title: "Delete Password",
                message: "Are you sure you want to delete this password?",
                preferredStyle: .alert
            )
            
            alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
            alert.addAction(UIAlertAction(title: "Delete", style: .destructive) { [weak self] _ in
                if let password = self?.password {
                    PasswordManager.shared.deletePassword(password)
                    self?.navigationController?.popViewController(animated: true)
                }
            })
            
            present(alert, animated: true)
        }
    
    private func createUsernameSection() -> UIView {
        createDetailSection(
            title: "Username",
            value: password.username,
            showCopy: true
        )
    }
    
    private func createPasswordSection() -> UIView {
        let container = UIView()
        container.translatesAutoresizingMaskIntoConstraints = false
        
        let titleLabel = UILabel()
        titleLabel.font = .systemFont(ofSize: 14, weight: .medium)
        titleLabel.text = "Password"
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        
        let valueLabel = UILabel()
        valueLabel.font = .systemFont(ofSize: 16)
        valueLabel.text = "••••••••"
        valueLabel.translatesAutoresizingMaskIntoConstraints = false
        self.passwordValueLabel = valueLabel
        
        let buttonStack = UIStackView()
        buttonStack.axis = .horizontal
        buttonStack.spacing = 8
        buttonStack.translatesAutoresizingMaskIntoConstraints = false
        
        let toggleButton = UIButton(type: .system)
        toggleButton.setImage(UIImage(systemName: "eye"), for: .normal)
        toggleButton.addTarget(self, action: #selector(togglePasswordVisibility), for: .touchUpInside)
        self.passwordToggleButton = toggleButton
        
        let copyButton = UIButton(type: .system)
        copyButton.setImage(UIImage(systemName: "doc.on.doc"), for: .normal)
        copyButton.addTarget(self, action: #selector(copyPassword), for: .touchUpInside)
        
        buttonStack.addArrangedSubview(toggleButton)
        buttonStack.addArrangedSubview(copyButton)
        
        container.addSubview(titleLabel)
        container.addSubview(valueLabel)
        container.addSubview(buttonStack)
        
        NSLayoutConstraint.activate([
            titleLabel.topAnchor.constraint(equalTo: container.topAnchor),
            titleLabel.leadingAnchor.constraint(equalTo: container.leadingAnchor),
            titleLabel.trailingAnchor.constraint(equalTo: container.trailingAnchor),
            
            valueLabel.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 4),
            valueLabel.leadingAnchor.constraint(equalTo: container.leadingAnchor),
            valueLabel.trailingAnchor.constraint(equalTo: buttonStack.leadingAnchor, constant: -8),
            valueLabel.bottomAnchor.constraint(equalTo: container.bottomAnchor),
            
            buttonStack.centerYAnchor.constraint(equalTo: valueLabel.centerYAnchor),
            buttonStack.trailingAnchor.constraint(equalTo: container.trailingAnchor),
            buttonStack.widthAnchor.constraint(equalToConstant: 88),
            buttonStack.heightAnchor.constraint(equalToConstant: 44)
        ])
        
        return container
    }
    //MARK: - Copy of all values
    private func createCopySection() -> UIView {
            let container = UIView()
            container.translatesAutoresizingMaskIntoConstraints = false
            
            let copyButton = UIButton(type: .system)
            copyButton.setTitle("Copy Username & Password", for: .normal)
            copyButton.addTarget(self, action: #selector(copyBothValues), for: .touchUpInside)
            copyButton.translatesAutoresizingMaskIntoConstraints = false
            
            container.addSubview(copyButton)
            
            NSLayoutConstraint.activate([
                copyButton.topAnchor.constraint(equalTo: container.topAnchor),
                copyButton.leadingAnchor.constraint(equalTo: container.leadingAnchor),
                copyButton.trailingAnchor.constraint(equalTo: container.trailingAnchor),
                copyButton.bottomAnchor.constraint(equalTo: container.bottomAnchor),
                copyButton.heightAnchor.constraint(equalToConstant: 44)
            ])
            
            return container
        }
        
        @objc private func copyBothValues() {
            UIPasteboard.general.string = password.getCombinedCredentials()
            showCopyFeedback("Username and password copied")
            
            // Clear clipboard after 60 seconds
            DispatchQueue.main.asyncAfter(deadline: .now() + 60) {
                if UIPasteboard.general.string == self.password.getCombinedCredentials() {
                    UIPasteboard.general.string = ""
                }
            }
        }
    
    // MARK: - Authentication
    private var isAuthenticated: Bool {
        guard let lastAuth = lastAuthTime else { return false }
        return Date().timeIntervalSince(lastAuth) < authTimeoutInterval
    }
    
    private func authenticateIfNeeded(completion: @escaping (Bool) -> Void) {
        if isAuthenticated {
            completion(true)
            return
        }
        
        BiometricAuthManager.shared.authenticateUser(reason: "Authenticate to view password") { [weak self] result in
            DispatchQueue.main.async {
                switch result {
                case .success:
                    self?.lastAuthTime = Date()
                    completion(true)
                case .failure(let error):
                    self?.showAuthenticationError(error)
                    completion(false)
                }
            }
        }
    }
    
    // MARK: - Password Visibility
    private func updatePasswordVisibility() {
        guard let valueLabel = passwordValueLabel,
              let toggleButton = passwordToggleButton else { return }
        
        valueLabel.text = isPasswordVisible ? password.password : "••••••••"
        let imageName = isPasswordVisible ? "eye.slash" : "eye"
        toggleButton.setImage(UIImage(systemName: imageName), for: .normal)
        
        // Auto-hide after 30 seconds if visible
        if isPasswordVisible {
            DispatchQueue.main.asyncAfter(deadline: .now() + 30) { [weak self] in
                self?.isPasswordVisible = false
            }
        }
    }
    
    // MARK: - Actions
    @objc private func copyValue(_ sender: UIButton) {
        guard let section = sender.superview,
              let valueLabel = section.subviews.first(where: { $0 is UILabel && ($0 as? UILabel)?.font.pointSize == 16 }) as? UILabel,
              let value = valueLabel.text else { return }
        
        UIPasteboard.general.string = value
        showCopyFeedback("Username copied to clipboard")
    }
    
    @objc private func copyPassword() {
        authenticateIfNeeded { [weak self] success in
            guard success, let self = self else { return }
            
            UIPasteboard.general.string = self.password.password
            self.showCopyFeedback("Password copied to clipboard")
            
            // Clear clipboard after 60 seconds
            DispatchQueue.main.asyncAfter(deadline: .now() + 60) {
                if UIPasteboard.general.string == self.password.password {
                    UIPasteboard.general.string = ""
                }
            }
        }
    }
    
    @objc private func togglePasswordVisibility() {
        // If currently visible, allow hiding without authentication
        if isPasswordVisible {
            isPasswordVisible = false
            return
        }
        
        // If not visible, require authentication before showing
        BiometricAuthManager.shared.authenticateUser(reason: "Authenticate to view password") { [weak self] result in
            DispatchQueue.main.async {
                switch result {
                case .success:
                    self?.isPasswordVisible = true
                case .failure(let error):
                    self?.showAuthenticationError(error)
                }
            }
        }
    }
    
    // MARK: - Feedback
    private func showCopyFeedback(_ message: String) {
        let feedbackGenerator = UINotificationFeedbackGenerator()
        feedbackGenerator.notificationOccurred(.success)
        
        let alert = UIAlertController(title: nil, message: message, preferredStyle: .alert)
        present(alert, animated: true)
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            alert.dismiss(animated: true)
        }
    }
    
    private func showAuthenticationError(_ error: Error) {
        let message: String
        if let authError = error as? BiometricAuthManager.AuthError {
            message = authError.errorDescription ?? error.localizedDescription
        } else {
            message = error.localizedDescription
        }
        
        let alert = UIAlertController(
            title: "Authentication Failed",
            message: message,
            preferredStyle: .alert
        )
        alert.addAction(UIAlertAction(title: "OK", style: .default))
        present(alert, animated: true)
    }
    
    // MARK: - Theme
    @objc private func themeDidChange(_ notification: Notification) {
            if let theme = notification.userInfo?["theme"] as? AppTheme {
                applyTheme(theme)
            } else {
                applyTheme(ThemeManager.loadTheme())
            }
        }
    
    private func applyTheme(_ theme: AppTheme) {
        view.backgroundColor = theme.backgroundColor
        titleLabel.textColor = theme.textColor
        
        stackView.arrangedSubviews.forEach { view in
            view.subviews.forEach { subview in
                if let label = subview as? UILabel {
                    label.textColor = theme.textColor
                } else if let button = subview as? UIButton {
                    button.tintColor = theme.accentColor
                } else if let stack = subview as? UIStackView {
                    stack.arrangedSubviews.forEach { buttonView in
                        (buttonView as? UIButton)?.tintColor = theme.accentColor
                    }
                }
            }
        }
    }
}
