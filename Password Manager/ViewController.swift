import UIKit
import UniformTypeIdentifiers
import LocalAuthentication

//protocol ViewControllerDelegate: AnyObject {
//    func applyTheme()
//    func backupPasswords()
//    func restorePasswords()
//}

class ViewController: UIViewController {
    // MARK: - Properties
    private var tableView: UITableView!
    private var searchBar: UISearchBar!
    private var emptyStateLabel: UILabel!
    private var allPasswords: [Password] = []
    private var filteredPasswords: [Password] = []
    private var isAuthenticating = false
    private var hasLoadedPasswords = false
    
    // MARK: - Lifecycle Methods
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupNavigationBar()
        applyCurrentTheme()
        setupNotifications()
        hasLoadedPasswords = false
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        if !hasLoadedPasswords {
            authenticateUser()
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
        setupSearchBar()
        setupTableView()
        setupEmptyStateLabel()
    }
    
    private func setupSearchBar() {
        searchBar = UISearchBar()
        searchBar.delegate = self
        searchBar.placeholder = "Search passwords"
        searchBar.searchBarStyle = .minimal
        searchBar.backgroundColor = .clear
        
        searchBar.searchTextField.backgroundColor = .systemGray6
        searchBar.searchTextField.textColor = ThemeManager.loadTheme().textColor
        
        view.addSubview(searchBar)
        
        searchBar.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            searchBar.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 10),
            searchBar.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            searchBar.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            searchBar.heightAnchor.constraint(equalToConstant: 44)
        ])
    }
    
    private func setupTableView() {
        tableView = UITableView(frame: .zero, style: .plain)
        tableView.backgroundColor = .clear
        tableView.separatorStyle = .none
        tableView.dataSource = self
        tableView.delegate = self
        tableView.register(PasswordCell.self, forCellReuseIdentifier: "PasswordCell")
        tableView.rowHeight = UITableView.automaticDimension
        tableView.estimatedRowHeight = 80
        view.addSubview(tableView)
        
        tableView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            tableView.topAnchor.constraint(equalTo: searchBar.bottomAnchor, constant: 10),
            tableView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            tableView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            tableView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -20)
        ])
    }
    
    private func setupEmptyStateLabel() {
        emptyStateLabel = UILabel()
        emptyStateLabel.text = "No passwords saved yet.\nTap '+' to add one."
        emptyStateLabel.textAlignment = .center
        emptyStateLabel.numberOfLines = 0
        view.addSubview(emptyStateLabel)
        
        emptyStateLabel.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            emptyStateLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            emptyStateLabel.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            emptyStateLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 40),
            emptyStateLabel.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -40)
        ])
    }
    
    private func setupNavigationBar() {
        let addButton = UIBarButtonItem(barButtonSystemItem: .add, target: self, action: #selector(addPassword))
        let settingsButton = UIBarButtonItem(image: UIImage(systemName: "gear"), style: .plain, target: self, action: #selector(showSettings))
        navigationItem.rightBarButtonItems = [addButton, settingsButton]
    }
    
    // MARK: - Authentication
    private func authenticateUser() {
        guard !isAuthenticating else { return }
        isAuthenticating = true
        
        BiometricAuthManager.shared.authenticateUser(reason: "Unlock Password Manager") { [weak self] result in
            DispatchQueue.main.async {
                self?.isAuthenticating = false
                
                switch result {
                case .success:
                    self?.hasLoadedPasswords = true
                    self?.loadPasswords()
                    
                case .failure(let error):
                    switch error {
                    case .biometricError(let laError) where laError.code == .userFallback,
                            .biometricError(let laError) where laError.code == .biometryLockout:
                        self?.showPasscodeAuthentication()
                    default:
                        self?.showAuthenticationFailedAlert()
                    }
                }
            }
        }
    }
    private func showAuthenticationFailedAlert() {
            let alert = UIAlertController(
                title: "Authentication Failed",
                message: "Unable to verify your identity. Please try again.",
                preferredStyle: .alert
            )
            alert.addAction(UIAlertAction(title: "Try Again", style: .default) { [weak self] _ in
                self?.authenticateUser()
            })
            alert.addAction(UIAlertAction(title: "Use Passcode", style: .default) { [weak self] _ in
                self?.showPasscodeAuthentication()
            })
            present(alert, animated: true)
        }
        
        private func showPasscodeAuthentication() {
            let alert = UIAlertController(
                title: "Enter Passcode",
                message: "Please enter your passcode to unlock the app",
                preferredStyle: .alert
            )
            
            alert.addTextField { textField in
                textField.isSecureTextEntry = true
                textField.placeholder = "Enter passcode"
                textField.rightView = self.createPasswordToggleButton(for: textField)
                textField.rightViewMode = .always
            }
            
            alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
            alert.addAction(UIAlertAction(title: "Unlock", style: .default) { [weak self] _ in
                self?.hasLoadedPasswords = true
                self?.loadPasswords()
            })
            
            present(alert, animated: true)
        }
        
        // MARK: - Data Management
//        private func loadPasswords() {
//            allPasswords = PasswordManager.shared.getAllPasswords()
//            filterPasswords(with: searchBar.text ?? "")
//            updateEmptyState()
//        }
    private func loadPasswords() {
        allPasswords = PasswordManager.shared.getAllPasswords()
        filterPasswords(with: searchBar.text ?? "")
        updateEmptyState()
    }
        
        private func filterPasswords(with searchText: String) {
            if searchText.isEmpty {
                filteredPasswords = allPasswords
            } else {
                filteredPasswords = allPasswords.filter { password in
                    password.title.lowercased().contains(searchText.lowercased()) ||
                    password.username.lowercased().contains(searchText.lowercased())
                }
            }
            DispatchQueue.main.async { [weak self] in
                self?.tableView.reloadData()
                self?.updateEmptyState()
                self?.applyCurrentTheme()
            }
        }
        
        private func updateEmptyState() {
            emptyStateLabel.isHidden = !allPasswords.isEmpty
            tableView.isHidden = allPasswords.isEmpty
            tableView.reloadData()
        }
        
        // MARK: - Theme Management
        @objc private func themeDidChange(_ notification: Notification) {
            if notification.userInfo?["theme"] is AppTheme {
                applyCurrentTheme()
            }
        }
        
        private func applyCurrentTheme() {
            let theme = ThemeManager.loadTheme()
            
            view.backgroundColor = theme.backgroundColor
            searchBar.backgroundColor = .clear
            searchBar.tintColor = theme.accentColor
            
            searchBar.searchTextField.backgroundColor = .systemGray6
            searchBar.searchTextField.textColor = theme.textColor
            searchBar.searchTextField.attributedPlaceholder = NSAttributedString(
                string: "Search passwords",
                attributes: [NSAttributedString.Key.foregroundColor: theme.textColor.withAlphaComponent(0.6)]
            )
            
            emptyStateLabel.textColor = theme.textColor
            tableView.backgroundColor = theme.backgroundColor
            
            tableView.visibleCells.forEach { cell in
                guard let passwordCell = cell as? PasswordCell,
                      let indexPath = tableView.indexPath(for: cell),
                      indexPath.row < filteredPasswords.count else { return }
                
                let password = filteredPasswords[indexPath.row]
                passwordCell.configure(with: password)
            }
        }
        
        // MARK: - Actions
    @objc private func addPassword() {
        let passwordFormVC = PasswordFormViewController()
        passwordFormVC.onSave = { [weak self] newPassword in
            PasswordManager.shared.savePassword(newPassword)
            self?.loadPasswords()  // Changed from reloadPasswords
        }
        navigationController?.pushViewController(passwordFormVC, animated: true)
    }
        
        @objc private func showSettings() {
            let settingsVC = SettingsViewController(style: .grouped)
            settingsVC.delegate = self
            navigationController?.pushViewController(settingsVC, animated: true)
        }
        
        // MARK: - Helper Methods
        private func createPasswordToggleButton(for textField: UITextField) -> UIButton {
            let button = UIButton(type: .custom)
            button.setImage(UIImage(systemName: "eye.slash"), for: .normal)
            button.frame = CGRect(x: 0, y: 0, width: 30, height: 30)
            button.tag = textField.tag
            button.addTarget(self, action: #selector(togglePasswordVisibility(_:)), for: .touchUpInside)
            return button
        }
        
        @objc private func togglePasswordVisibility(_ sender: UIButton) {
            guard let alertController = presentedViewController as? UIAlertController,
                  let textField = alertController.textFields?[sender.tag] else { return }
            
            textField.isSecureTextEntry.toggle()
            let imageName = textField.isSecureTextEntry ? "eye.slash" : "eye"
            sender.setImage(UIImage(systemName: imageName), for: .normal)
        }
        
        private func showAlert(title: String, message: String) {
            let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .default))
            present(alert, animated: true)
        }
        
        // MARK: - Backup and Restore Methods
        private func createAndShareBackup(withPassword password: String) {
            if let backupURL = BackupManager.shared.createBackup(passwords: allPasswords, withPassword: password) {
                let activityVC = UIActivityViewController(activityItems: [backupURL], applicationActivities: nil)
                activityVC.completionWithItemsHandler = { [weak self] (activityType, completed, returnedItems, error) in
                    if completed {
                        self?.showDeleteAllPasswordsPrompt()
                    }
                }
                present(activityVC, animated: true)
            } else {
                showAlert(title: "Error", message: "Failed to create backup")
            }
        }
        
    private func showDeleteAllPasswordsPrompt() {
        let alert = UIAlertController(
            title: "Delete All Passwords",
            message: "Do you want to delete all saved passwords? This action cannot be undone.",
            preferredStyle: .alert
        )
        
        alert.addAction(UIAlertAction(title: "Delete All", style: .destructive) { [weak self] _ in
            self?.allPasswords.removeAll()
            self?.filteredPasswords.removeAll()
            PasswordManager.shared.deleteAllPasswords()
            self?.tableView.reloadData()
            self?.updateEmptyState()
            self?.showAlert(title: "Success", message: "All passwords have been deleted")
        })
        
        alert.addAction(UIAlertAction(title: "Keep Passwords", style: .cancel))
        
        present(alert, animated: true)
    }
        
        private func promptForRestorePassword(fileURL: URL) {
            let alert = UIAlertController(
                title: "Enter Backup Password",
                message: "Please enter the password you used to create this backup",
                preferredStyle: .alert
            )
            
            alert.addTextField { textField in
                textField.isSecureTextEntry = true
                textField.placeholder = "Enter password"
                textField.rightView = self.createPasswordToggleButton(for: textField)
                textField.rightViewMode = .always
            }
            
            alert.addAction(UIAlertAction(title: "Restore", style: .default) { [weak self] _ in
                guard let password = alert.textFields?.first?.text,
                      !password.isEmpty else {
                    self?.showAlert(title: "Error", message: "Password cannot be empty")
                    return
                }
                
                self?.performRestore(fileURL: fileURL, password: password)
            })
            
            alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
            
            present(alert, animated: true)
        }
        
    private func performRestore(fileURL: URL, password: String) {
        do {
            let fileData = try Data(contentsOf: fileURL)
            if let restoredPasswords = BackupManager.shared.restoreBackup(data: fileData, withPassword: password) {
                PasswordManager.shared.replaceAllPasswords(with: restoredPasswords)
                loadPasswords()  // Changed from reloadPasswords
                showAlert(title: "Success", message: "Passwords restored successfully")
            } else {
                showAlert(title: "Error", message: "Failed to restore passwords. Make sure you entered the correct password.")
            }
        } catch {
            showAlert(title: "Error", message: "Failed to read backup file: \(error.localizedDescription)")
        }
    }
    }

    // MARK: - UITableViewDataSource
    extension ViewController: UITableViewDataSource {
        func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
            return filteredPasswords.count
        }
        
        func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
            let cell = tableView.dequeueReusableCell(withIdentifier: "PasswordCell", for: indexPath) as! PasswordCell
            if indexPath.row < filteredPasswords.count {
                let password = filteredPasswords[indexPath.row]
                cell.configure(with: password)
            }
            return cell
        }
    }

    // MARK: - UITableViewDelegate
    extension ViewController: UITableViewDelegate {
        func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
            tableView.deselectRow(at: indexPath, animated: true)
            guard indexPath.row < filteredPasswords.count else { return }
            
            let password = filteredPasswords[indexPath.row]
            let detailVC = PasswordDetailViewController(password: password)
            navigationController?.pushViewController(detailVC, animated: true)
        }
    }

    // MARK: - UISearchBarDelegate
    extension ViewController: UISearchBarDelegate {
        func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
            filterPasswords(with: searchText)
        }
        
        func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
            searchBar.resignFirstResponder()
        }
    }

    // MARK: - ViewControllerDelegate
    extension ViewController: ViewControllerDelegate {
        func applyTheme() {
            applyCurrentTheme()
        }
        
        func backupPasswords() {
            let alert = UIAlertController(
                title: "Backup Passwords",
                message: "Enter a password to protect your backup. You'll need this password to restore your passwords later.",
                preferredStyle: .alert
            )
            
            alert.addTextField { textField in
                textField.isSecureTextEntry = true
                textField.placeholder = "Enter password"
                textField.tag = 0
                textField.rightView = self.createPasswordToggleButton(for: textField)
                textField.rightViewMode = .always
            }
            
            alert.addTextField { textField in
                textField.isSecureTextEntry = true
                textField.placeholder = "Confirm password"
                textField.tag = 1
                textField.rightView = self.createPasswordToggleButton(for: textField)
                textField.rightViewMode = .always
            }
            
            alert.addAction(UIAlertAction(title: "Create Backup", style: .default) { [weak self] _ in
                guard let password = alert.textFields?[0].text,
                      let confirmPassword = alert.textFields?[1].text,
                      !password.isEmpty,
                      password == confirmPassword else {
                    self?.showAlert(title: "Error", message: "Passwords do not match or are empty")
                    return
                }
                
                self?.createAndShareBackup(withPassword: password)
            })
            
            alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
            
            present(alert, animated: true)
        }
        
        func restorePasswords() {
            if #available(iOS 14.0, *) {
                let documentPicker = UIDocumentPickerViewController(forOpeningContentTypes: [.data], asCopy: true)
                documentPicker.delegate = self
                documentPicker.allowsMultipleSelection = false
                present(documentPicker, animated: true)
            } else {
                let documentPicker = UIDocumentPickerViewController(documentTypes: ["public.data"], in: .import)
                documentPicker.delegate = self
                documentPicker.allowsMultipleSelection = false
                present(documentPicker, animated: true)
            }
        }
    }

    // MARK: - UIDocumentPickerDelegate
    extension ViewController: UIDocumentPickerDelegate {
        func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
            guard let selectedFileURL = urls.first else {
                showAlert(title: "Error", message: "No file selected")
                return
            }
            promptForRestorePassword(fileURL: selectedFileURL)
        }
    }


/*
import UIKit
import UniformTypeIdentifiers
import LocalAuthentication

class ViewController: UIViewController, UITableViewDataSource, UITableViewDelegate, UISearchBarDelegate, UIDocumentPickerDelegate {
    var tableView: UITableView!
    var tableViewHeightConstraint: NSLayoutConstraint!
    var searchBar: UISearchBar!
    private var hasLoadedPasswords = false
    var allPasswords: [Password] = []
    var filteredPasswords: [Password] = []
    var emptyStateLabel: UILabel?
    private var isAuthenticating = false
    private let cellHeight: CGFloat = 44
    private let cellMargin: CGFloat = 8
    
    override func viewDidLoad() {
        super.viewDidLoad()
        print("ViewController viewDidLoad called")
        
        setupUI()
        updateBarButtonItems()
        applyTheme()
    }
    
    // Replace viewDidAppear with this simplified version
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        if !hasLoadedPasswords {
            authenticateUser()
        }
    }
//      private func testFaceID() {
//            let context = LAContext()
//            var error: NSError?
//            
//            if context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) {
//                let reason = "Test Face ID"
//                context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: reason) { success, error in
//                    DispatchQueue.main.async {
//                        if success {
//                            print("Face ID authentication successful")
//                        } else {
//                            print("Face ID authentication failed: \(String(describing: error))")
//                        }
//                    }
//                }
//            } else {
//                print("Face ID not available: \(String(describing: error))")
//            }
//        }
    
    private func authenticateUser() {
            guard !isAuthenticating else { return }
            isAuthenticating = true
            
            let (canUseBiometrics, errorMessage) = BiometricAuthManager.shared.canUseBiometrics()
            
            if canUseBiometrics {
                BiometricAuthManager.shared.authenticateUser(reason: "Unlock Password Manager") { success, error in
                    self.isAuthenticating = false
                    if success {
                        print("Authentication successful")
                        self.loadPasswords()
                    } else {
                        print("Authentication failed: \(String(describing: error))")
                        if let error = error as? LAError {
                            switch error.code {
                            case .userCancel, .systemCancel, .appCancel:
                                print("Authentication was cancelled")
                                // Optionally retry or show alternative method
                                self.showAlternativeAuthenticationMethod()
                            case .userFallback:
                                print("User chose to use alternative method")
                                self.showAlternativeAuthenticationMethod()
                            default:
                                self.showAuthenticationFailedAlert()
                            }
                        } else {
                            self.showAuthenticationFailedAlert()
                        }
                    }
                }
            } else {
                isAuthenticating = false
                print("Cannot use biometrics: \(errorMessage)")
                self.showAlternativeAuthenticationMethod()
            }
        }
    
    private func showAuthenticationFailedAlert() {
            let alert = UIAlertController(title: "Authentication Failed", message: "Unable to verify your identity. Please try again.", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Try Again", style: .default) { _ in
                self.authenticateUser()
            })
            alert.addAction(UIAlertAction(title: "Use Alternative Method", style: .default) { _ in
                self.showAlternativeAuthenticationMethod()
            })
            self.present(alert, animated: true, completion: nil)
        }

    private func showAlternativeAuthenticationMethod() {
           print("Showing alternative authentication method")
           // Implement your alternative authentication method here
           // For now, we'll just load the passwords
           self.loadPasswords()
       }
    
    private func loadPasswords() {
            // Load passwords here
            // This is where you'd typically call your existing password loading logic
            reloadPasswords()
        }
    
    func setupUI() {
        view.backgroundColor = .systemRed
        
        // Create search bar
        searchBar = UISearchBar()
        searchBar.delegate = self
        searchBar.placeholder = "Search passwords"
        searchBar.backgroundColor = .white
        searchBar.layer.cornerRadius = 10
        searchBar.clipsToBounds = true
        view.addSubview(searchBar)
        
        // Set up constraints for search bar
        searchBar.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            searchBar.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 10),
            searchBar.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            searchBar.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            searchBar.heightAnchor.constraint(equalToConstant: 44)
        ])
        
        // Create table view
        tableView = UITableView(frame: .zero, style: .plain)
        tableView.backgroundColor = .white
        tableView.layer.cornerRadius = 10
        tableView.clipsToBounds = true
        tableView.dataSource = self
        tableView.delegate = self
        tableView.register(UITableViewCell.self, forCellReuseIdentifier: "PasswordCell")
        tableView.alwaysBounceVertical = false
        view.addSubview(tableView)
        
        // Set up constraints for table view
        tableView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            tableView.topAnchor.constraint(equalTo: searchBar.bottomAnchor, constant: 10),
            tableView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            tableView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20)
        ])
        
        // Add height constraint for table view
        tableViewHeightConstraint = tableView.heightAnchor.constraint(equalToConstant: 0)
        tableViewHeightConstraint.isActive = true
        
        // Create empty state label
        emptyStateLabel = UILabel()
        emptyStateLabel?.text = "No passwords saved yet.\n\nTap '+' to add one."
        emptyStateLabel?.textAlignment = .center
        emptyStateLabel?.textColor = .white
        emptyStateLabel?.numberOfLines = 0
        emptyStateLabel?.font = UIFont.systemFont(ofSize: 20, weight: .medium)
        emptyStateLabel?.isHidden = true
        if let emptyStateLabel = emptyStateLabel {
            view.addSubview(emptyStateLabel)
            emptyStateLabel.translatesAutoresizingMaskIntoConstraints = false
            NSLayoutConstraint.activate([
                emptyStateLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),
                emptyStateLabel.centerYAnchor.constraint(equalTo: view.centerYAnchor),
                emptyStateLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 40),
                emptyStateLabel.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -40)
            ])
        }
        
        print("UI setup completed")
    }
    
    func updateTableViewHeight() {
        let maxHeight = view.frame.height - view.safeAreaInsets.top - view.safeAreaInsets.bottom - searchBar.frame.height - 60
        let contentHeight = CGFloat(filteredPasswords.count * 44) // Assuming each cell is 44 points tall
        let newHeight = min(maxHeight, contentHeight)
        
        tableViewHeightConstraint.constant = newHeight
        view.layoutIfNeeded()
        
        // Enable or disable scrolling based on content size
        tableView.isScrollEnabled = contentHeight > maxHeight
    }
    
    func updateBarButtonItems() {
        let addButton = UIBarButtonItem(barButtonSystemItem: .add, target: self, action: #selector(addPassword))
        let settingsButton = UIBarButtonItem(image: UIImage(systemName: "gear"), style: .plain, target: self, action: #selector(showSettings))
        
        navigationItem.rightBarButtonItems = [addButton, settingsButton]
    }
    
    @objc func showSettings() {
        let settingsVC = SettingsViewController(style: .grouped)
        settingsVC.delegate = self
        navigationController?.pushViewController(settingsVC, animated: true)
    }
    
    @objc func addPassword() {
        let passwordFormVC = PasswordFormViewController()
        passwordFormVC.onSave = { [weak self] newPassword in
            PasswordManager.shared.savePassword(newPassword)
            self?.reloadPasswords()
        }
        navigationController?.pushViewController(passwordFormVC, animated: true)
    }
    
    func showAlert(title: String, message: String) {
            let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .default))
            present(alert, animated: true)
        }
    
    private func updateEmptyStateMessage() {
            if allPasswords.isEmpty {
                let emptyStateLabel = UILabel()
                emptyStateLabel.text = "No passwords saved. Add a new password to get started."
                emptyStateLabel.textAlignment = .center
                emptyStateLabel.textColor = ThemeManager.loadTheme().textColor
                emptyStateLabel.numberOfLines = 0
                emptyStateLabel.frame = tableView.bounds
                tableView.backgroundView = emptyStateLabel
            } else {
                tableView.backgroundView = nil
            }
        }
    
    func reloadPasswords() {
            allPasswords = PasswordManager.shared.getAllPasswords()
            filterPasswords(with: searchBar.text ?? "")
            DispatchQueue.main.async { [weak self] in
                self?.updateEmptyStateMessage()
                self?.tableView.reloadData()
                self?.updateTableViewHeight()
            }
        }
    
    func filterPasswords(with searchText: String) {
        if searchText.isEmpty {
            filteredPasswords = allPasswords
        } else {
            filteredPasswords = PasswordSearchManager.shared.searchPasswords(allPasswords, with: searchText)
        }
        DispatchQueue.main.async { [weak self] in
            self?.tableView.reloadData()
            self?.updateTableViewHeight()
        }
        
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        if !hasLoadedPasswords {
            reloadPasswords()
            hasLoadedPasswords = true
        }
        print("viewWillAppear: Number of passwords: \(allPasswords.count)")
    }
    
    
    
    // MARK: - UITableViewDataSource
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return filteredPasswords.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
            let cell = tableView.dequeueReusableCell(withIdentifier: "PasswordCell", for: indexPath)
            
            if indexPath.row < filteredPasswords.count {
                let password = filteredPasswords[indexPath.row]
                cell.textLabel?.text = password.title
                cell.detailTextLabel?.text = password.username
                cell.accessoryType = .disclosureIndicator
                
                // Apply theme colors
                let theme = ThemeManager.loadTheme()
                cell.backgroundColor = theme.boxBackgroundColor
                cell.textLabel?.textColor = theme.textColor
                cell.detailTextLabel?.textColor = theme.textColor
            } else {
                cell.textLabel?.text = "Error"
                cell.detailTextLabel?.text = nil
                cell.accessoryType = .none
            }
            
            return cell
        }
    
    // MARK: - UITableViewDelegate
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if indexPath.row < filteredPasswords.count {
            let password = filteredPasswords[indexPath.row]
            editPassword(password)
        }
        tableView.deselectRow(at: indexPath, animated: true)
    }
    
    func editPassword(_ password: Password) {
        let passwordFormVC = PasswordFormViewController()
        passwordFormVC.passwordToEdit = password
        passwordFormVC.onSave = { [weak self] updatedPassword in
            PasswordManager.shared.updatePassword(updatedPassword)
            self?.reloadPasswords()
        }
        passwordFormVC.onDelete = { [weak self] in
            PasswordManager.shared.deletePassword(password)
            self?.reloadPasswords()
        }
        navigationController?.pushViewController(passwordFormVC, animated: true)
    }
    
    // MARK: - UISearchBarDelegate
    
    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        filterPasswords(with: searchText)
        tableView.reloadData()
        updateTableViewHeight()
    }
    
    func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
        searchBar.resignFirstResponder()
    }
}

// MARK: - ViewControllerDelegate
extension ViewController: ViewControllerDelegate {
    func applyTheme() {
        let currentTheme = ThemeManager.loadTheme()
        view.backgroundColor = currentTheme.backgroundColor
        tableView.backgroundColor = currentTheme.boxBackgroundColor
        searchBar.backgroundColor = currentTheme.boxBackgroundColor
        emptyStateLabel?.textColor = currentTheme.textColor
        
        // Refresh table view to update cell colors
        tableView.reloadData()
        
        DispatchQueue.main.async { [weak self] in
            self?.updateTableViewHeight()
            self?.view.setNeedsLayout()
            self?.view.layoutIfNeeded()
        }
    }
    func backupPasswords() {
            let alert = UIAlertController(title: "Backup Passwords", message: "Enter a password to protect your backup. You'll need this password to restore your passwords later.", preferredStyle: .alert)
            
            alert.addTextField { textField in
                textField.isSecureTextEntry = true
                textField.placeholder = "Enter password"
                textField.rightView = self.createPasswordToggleButton(for: textField)
                textField.rightViewMode = .always
            }
            
            alert.addTextField { textField in
                textField.isSecureTextEntry = true
                textField.placeholder = "Confirm password"
                textField.rightView = self.createPasswordToggleButton(for: textField)
                textField.rightViewMode = .always
            }
            
            alert.addAction(UIAlertAction(title: "Next", style: .default) { [weak self] _ in
                guard let password = alert.textFields?[0].text,
                      let confirmPassword = alert.textFields?[1].text,
                      password == confirmPassword, !password.isEmpty else {
                    self?.showAlert(title: "Error", message: "Passwords do not match or are empty")
                    return
                }
                
                self?.verifyBackupPassword(password)
            })
            
            alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
            
            present(alert, animated: true)
        }
    
    func restorePasswords() {
            if #available(iOS 14.0, *) {
                let documentPicker = UIDocumentPickerViewController(forOpeningContentTypes: [.data], asCopy: true)
                documentPicker.delegate = self
                documentPicker.allowsMultipleSelection = false
                present(documentPicker, animated: true, completion: nil)
            } else {
                // Fallback on earlier versions
                let documentPicker = UIDocumentPickerViewController(documentTypes: ["public.data"], in: .import)
                documentPicker.delegate = self
                documentPicker.allowsMultipleSelection = false
                present(documentPicker, animated: true, completion: nil)
            }
        }
    
    func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
            guard let selectedFileURL = urls.first else {
                showAlert(title: "Error", message: "No file selected")
                return
            }

            promptForRestorePassword(fileURL: selectedFileURL)
        }
    
    func verifyBackupPassword(_ password: String) {
            let verificationAlert = UIAlertController(title: "Verify Password", message: "Please re-enter your password to verify", preferredStyle: .alert)
            
            verificationAlert.addTextField { textField in
                textField.isSecureTextEntry = true
                textField.placeholder = "Re-enter password"
                textField.rightView = self.createPasswordToggleButton(for: textField)
                textField.rightViewMode = .always
            }
            
            verificationAlert.addAction(UIAlertAction(title: "Create Backup", style: .default) { [weak self] _ in
                guard let verifiedPassword = verificationAlert.textFields?[0].text,
                      verifiedPassword == password else {
                    self?.showAlert(title: "Error", message: "Password verification failed")
                    return
                }
                
                self?.createAndShareBackup(withPassword: password)
            })
            
            verificationAlert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
            
            present(verificationAlert, animated: true)
        }
    
    @objc private func togglePasswordVisibility(_ sender: UIButton) {
            guard let alertController = presentedViewController as? UIAlertController,
                  let textField = alertController.textFields?[sender.tag] else { return }
            
            textField.isSecureTextEntry.toggle()
            let imageName = textField.isSecureTextEntry ? "eye.slash" : "eye"
            sender.setImage(UIImage(systemName: imageName), for: .normal)
        }
    
    private func createPasswordToggleButton(for textField: UITextField) -> UIButton {
            let button = UIButton(type: .custom)
            button.setImage(UIImage(systemName: "eye.slash"), for: .normal)
            button.frame = CGRect(x: 0, y: 0, width: 30, height: 30)
            button.addTarget(self, action: #selector(togglePasswordVisibility(_:)), for: .touchUpInside)
            button.tag = textField.tag
            return button
        }
    
    private func createAndShareBackup(withPassword password: String) {
            if let backupURL = BackupManager.shared.createBackup(passwords: self.allPasswords, withPassword: password) {
                let activityVC = UIActivityViewController(activityItems: [backupURL], applicationActivities: nil)
                activityVC.completionWithItemsHandler = { [weak self] (activityType, completed, returnedItems, error) in
                    if completed {
                        self?.showDeleteAllPasswordsPrompt()
                    }
                }
                present(activityVC, animated: true)
            } else {
                showAlert(title: "Error", message: "Failed to create backup")
            }
        }
    
    private func showDeleteAllPasswordsPrompt() {
            let alert = UIAlertController(title: "Delete All Passwords", message: "Do you want to delete all saved passwords? This action cannot be undone.", preferredStyle: .alert)
            
            alert.addAction(UIAlertAction(title: "Delete All", style: .destructive) { [weak self] _ in
                self?.deleteAllPasswords()
            })
            
            alert.addAction(UIAlertAction(title: "Keep Passwords", style: .cancel))
            
            present(alert, animated: true)
        }

    private func deleteAllPasswords() {
            // Clear the data source
            self.allPasswords.removeAll()
            
            // Clear any filtered results
            self.filteredPasswords.removeAll()
            
            // Update UserDefaults or your persistent storage
            PasswordManager.shared.deleteAllPasswords()
            
            // Reload the table view
            DispatchQueue.main.async {
                    self.tableView.reloadData()
                
                // Update empty state
                self.updateEmptyState()
            }
            
            showAlert(title: "Success", message: "All passwords have been deleted")
        }
    
    private func updateEmptyState() {
            if allPasswords.isEmpty {
                // Show empty state
                let emptyStateLabel = UILabel()
                emptyStateLabel.text = "No passwords saved. Add a new password to get started."
                emptyStateLabel.textAlignment = .center
                emptyStateLabel.textColor = ThemeManager.loadTheme().textColor
                emptyStateLabel.numberOfLines = 0
                emptyStateLabel.frame = tableView.bounds
                tableView.backgroundView = emptyStateLabel
            } else {
                // Remove empty state
                tableView.backgroundView = nil
            }
        }
    
    private func promptForRestorePassword(fileURL: URL) {
            let alert = UIAlertController(title: "Enter Backup Password", message: "Please enter the password you used to create this backup", preferredStyle: .alert)
            
            alert.addTextField { textField in
                textField.isSecureTextEntry = true
                textField.placeholder = "Enter password"
            }
            
            let restoreAction = UIAlertAction(title: "Restore", style: .default) { [weak self] _ in
                guard let password = alert.textFields?.first?.text, !password.isEmpty else {
                    self?.showAlert(title: "Error", message: "Password cannot be empty")
                    return
                }
                
                self?.performRestore(fileURL: fileURL, password: password)
            }
            
            alert.addAction(restoreAction)
            alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
            
            present(alert, animated: true, completion: nil)
        }
    
    private func performRestore(fileURL: URL, password: String) {
            do {
                let fileData = try Data(contentsOf: fileURL)
                if let restoredPasswords = BackupManager.shared.restoreBackup(data: fileData, withPassword: password) {
                    PasswordManager.shared.replaceAllPasswords(with: restoredPasswords)
                    reloadPasswords()
                    showAlert(title: "Success", message: "Passwords restored successfully")
                } else {
                    showAlert(title: "Error", message: "Failed to restore passwords. Make sure you entered the correct password.")
                }
            } catch {
                showAlert(title: "Error", message: "Failed to read backup file: \(error.localizedDescription)")
            }
        }
}
*/
