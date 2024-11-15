import UIKit

class PasswordFormViewController: UIViewController {
    // MARK: - Properties
    private let scrollView: UIScrollView = {
        let scrollView = UIScrollView()
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        scrollView.keyboardDismissMode = .onDrag
        return scrollView
    }()
    
    let titleTextField = UITextField()
    let usernameTextField = UITextField()
    let passwordTextField = UITextField()
    let viewPasswordButton = UIButton(type: .system)
    let deleteButton = UIButton(type: .system)
    let notesTextView: UITextView = {
        let textView = UITextView()
        textView.font = .systemFont(ofSize: 16)
        textView.layer.borderColor = UIColor.systemGray4.cgColor
        textView.layer.borderWidth = 1
        textView.layer.cornerRadius = 8
        textView.translatesAutoresizingMaskIntoConstraints = false
        return textView
    }()
    
    var onSave: ((Password) -> Void)?
    var onDelete: (() -> Void)?
    var passwordToEdit: Password?
    
    // Track original values
    private var originalTitle: String = ""
    private var originalUsername: String = ""
    private var originalPassword: String = ""
    private var originalNotes: String = ""
    
    private var hasUnsavedChanges: Bool {
        let currentTitle = titleTextField.text ?? ""
        let currentUsername = usernameTextField.text ?? ""
        let currentPassword = passwordTextField.text ?? ""
        let currentNotes = notesTextView.text ?? ""
        
        return currentTitle != originalTitle ||
               currentUsername != originalUsername ||
               currentPassword != originalPassword ||
               currentNotes != originalNotes
    }
    
    private var isValidForm: Bool {
        guard let title = titleTextField.text,
              let username = usernameTextField.text,
              let password = passwordTextField.text else {
            return false
        }
        return !title.isEmpty && !username.isEmpty && !password.isEmpty
    }
    
    // MARK: - Lifecycle
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupNavigationBar()
        setupDelegates()
        setupKeyboardHandling()
        storeOriginalValues()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        updateSaveButtonState()
    }
    
    // MARK: - Setup Methods
    private func setupUI() {
        view.backgroundColor = .systemBackground
        
        // Add scroll view
        view.addSubview(scrollView)
        
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 20
        stackView.translatesAutoresizingMaskIntoConstraints = false
        scrollView.addSubview(stackView)
        
        // Configure text fields
        [titleTextField, usernameTextField, passwordTextField].forEach { textField in
            textField.borderStyle = .roundedRect
            textField.addTarget(self, action: #selector(textFieldDidChange), for: .editingChanged)
            stackView.addArrangedSubview(textField)
        }
        
        titleTextField.placeholder = "Title"
        usernameTextField.placeholder = "Username"
        usernameTextField.autocapitalizationType = .none
        passwordTextField.placeholder = "Password"
        passwordTextField.isSecureTextEntry = true
        
        // Configure view password button
        viewPasswordButton.setTitle("View Password", for: .normal)
        viewPasswordButton.addTarget(self, action: #selector(togglePasswordVisibility), for: .touchUpInside)
        viewPasswordButton.isHidden = true
        stackView.addArrangedSubview(viewPasswordButton)
        
        // Configure notes
        let notesLabel = UILabel()
        notesLabel.text = "Notes (Optional)"
        stackView.addArrangedSubview(notesLabel)
        stackView.addArrangedSubview(notesTextView)
        
        // Add delete button if editing
        if passwordToEdit != nil {
            title = "Edit Password"
            deleteButton.setTitle("Delete", for: .normal)
            deleteButton.setTitleColor(.red, for: .normal)
            deleteButton.addTarget(self, action: #selector(deletePassword), for: .touchUpInside)
            stackView.addArrangedSubview(deleteButton)
        } else {
            title = "Add Password"
        }
        
        // Fill existing data if editing
        if let password = passwordToEdit {
            titleTextField.text = password.title
            usernameTextField.text = password.username
            passwordTextField.text = password.password
            notesTextView.text = password.notes
            viewPasswordButton.isHidden = password.password.isEmpty
        }
        
        // Setup constraints
        NSLayoutConstraint.activate([
            scrollView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            scrollView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            
            stackView.topAnchor.constraint(equalTo: scrollView.topAnchor, constant: 20),
            stackView.leadingAnchor.constraint(equalTo: scrollView.leadingAnchor, constant: 20),
            stackView.trailingAnchor.constraint(equalTo: scrollView.trailingAnchor, constant: -20),
            stackView.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor, constant: -20),
            stackView.widthAnchor.constraint(equalTo: scrollView.widthAnchor, constant: -40),
            
            notesTextView.heightAnchor.constraint(greaterThanOrEqualToConstant: 100)
        ])
    }
    
    private func setupNavigationBar() {
        // Add save button to navigation bar
        let saveButton = UIBarButtonItem(title: "Save", style: .done, target: self, action: #selector(savePassword))
        navigationItem.rightBarButtonItem = saveButton
        
        // Handle back button
        navigationItem.leftBarButtonItem = UIBarButtonItem(
            title: "Back",
            style: .plain,
            target: self,
            action: #selector(backButtonTapped)
        )
    }
    
    private func setupDelegates() {
        titleTextField.delegate = self
        usernameTextField.delegate = self
        passwordTextField.delegate = self
        notesTextView.delegate = self
    }
    
    private func setupKeyboardHandling() {
        // Add tap gesture to dismiss keyboard
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(dismissKeyboard))
        view.addGestureRecognizer(tapGesture)
        
        // Register for keyboard notifications
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide), name: UIResponder.keyboardWillHideNotification, object: nil)
    }
    
    private func storeOriginalValues() {
        originalTitle = titleTextField.text ?? ""
        originalUsername = usernameTextField.text ?? ""
        originalPassword = passwordTextField.text ?? ""
        originalNotes = notesTextView.text ?? ""
    }
    
    private func updateSaveButtonState() {
        navigationItem.rightBarButtonItem?.isEnabled = isValidForm
    }
    
    // MARK: - Actions
    @objc private func backButtonTapped() {
        if hasUnsavedChanges {
            showUnsavedChangesAlert()
        } else {
            navigationController?.popViewController(animated: true)
        }
    }
    
    @objc private func textFieldDidChange(_ textField: UITextField) {
        if textField == passwordTextField {
            viewPasswordButton.isHidden = passwordTextField.text?.isEmpty ?? true
        }
        updateSaveButtonState()
    }
    
    @objc func togglePasswordVisibility() {
        passwordTextField.isSecureTextEntry.toggle()
        let buttonTitle = passwordTextField.isSecureTextEntry ? "View Password" : "Hide Password"
        viewPasswordButton.setTitle(buttonTitle, for: .normal)
    }
    
    @objc func savePassword() {
        guard isValidForm,
              let title = titleTextField.text,
              let username = usernameTextField.text,
              let password = passwordTextField.text else {
            showValidationAlert()
            return
        }
        
        let notes = notesTextView.text.trimmingCharacters(in: .whitespacesAndNewlines)
        let newPassword = Password(
            id: passwordToEdit?.id ?? UUID().uuidString,
            title: title,
            username: username,
            password: password,
            notes: notes.isEmpty ? nil : notes
        )
        
        onSave?(newPassword)
        navigationController?.popViewController(animated: true)
    }
    
    @objc func deletePassword() {
        let alertController = UIAlertController(
            title: "Delete Password",
            message: "Are you sure you want to delete this password?",
            preferredStyle: .alert
        )
        
        alertController.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        alertController.addAction(UIAlertAction(title: "Delete", style: .destructive) { [weak self] _ in
            self?.onDelete?()
            self?.navigationController?.popViewController(animated: true)
        })
        
        present(alertController, animated: true)
    }
    
    @objc private func dismissKeyboard() {
        view.endEditing(true)
    }
    
    @objc private func keyboardWillShow(notification: NSNotification) {
            guard let keyboardFrame = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect else {
                return
            }
            
            scrollView.contentInset.bottom = keyboardFrame.height
            scrollView.verticalScrollIndicatorInsets.bottom = keyboardFrame.height
        }
        
        @objc private func keyboardWillHide(notification: NSNotification) {
            scrollView.contentInset.bottom = 0
            scrollView.verticalScrollIndicatorInsets.bottom = 0
        }
    
    // MARK: - Alert Helpers
    private func showUnsavedChangesAlert() {
        let alert = UIAlertController(
            title: "Unsaved Changes",
            message: "You have unsaved changes. Would you like to save them?",
            preferredStyle: .alert
        )
        
        alert.addAction(UIAlertAction(title: "Save", style: .default) { [weak self] _ in
            self?.savePassword()
        })
        
        alert.addAction(UIAlertAction(title: "Discard", style: .destructive) { [weak self] _ in
            self?.navigationController?.popViewController(animated: true)
        })
        
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        
        present(alert, animated: true)
    }
    
    private func showValidationAlert() {
        let alert = UIAlertController(
            title: "Required Fields",
            message: "Please fill in all required fields (Title, Username, and Password)",
            preferredStyle: .alert
        )
        alert.addAction(UIAlertAction(title: "OK", style: .default))
        present(alert, animated: true)
    }
}

// MARK: - UITextFieldDelegate
extension PasswordFormViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        switch textField {
        case titleTextField:
            usernameTextField.becomeFirstResponder()
        case usernameTextField:
            passwordTextField.becomeFirstResponder()
        case passwordTextField:
            notesTextView.becomeFirstResponder()
        default:
            textField.resignFirstResponder()
        }
        return true
    }
}

// MARK: - UITextViewDelegate
extension PasswordFormViewController: UITextViewDelegate {
    func textViewDidChange(_ textView: UITextView) {
        // No validation needed for notes as it's optional
    }
}


/*
import UIKit

class PasswordFormViewController: UIViewController {
    let titleTextField = UITextField()
    let usernameTextField = UITextField()
    let passwordTextField = UITextField()
    let viewPasswordButton = UIButton(type: .system)
    let saveButton = UIButton(type: .system)
    let deleteButton = UIButton(type: .system)
    let notesTextView: UITextView = {
        let textView = UITextView()
        textView.font = .systemFont(ofSize: 16)
        textView.layer.borderColor = UIColor.systemGray4.cgColor
        textView.layer.borderWidth = 1
        textView.layer.cornerRadius = 8
        textView.translatesAutoresizingMaskIntoConstraints = false
        return textView
    }()
    var onSave: ((Password) -> Void)?
    var onDelete: (() -> Void)?
    var passwordToEdit: Password?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupDelegates()
    }
    
    func setupUI() {
        view.backgroundColor = .white
        
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 20
        stackView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(stackView)
        let notesLabel = UILabel()
        notesLabel.text = "Notes (Optional)"
        stackView.addArrangedSubview(notesLabel)
        stackView.addArrangedSubview(notesTextView)
        
        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 20),
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            stackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            notesTextView.heightAnchor.constraint(greaterThanOrEqualToConstant: 100)
        ])
        if let password = passwordToEdit {
            titleTextField.text = password.title
            usernameTextField.text = password.username
            passwordTextField.text = password.password
            notesTextView.text = password.notes
            viewPasswordButton.isHidden = password.password.isEmpty
        }
        [titleTextField, usernameTextField, passwordTextField].forEach { textField in
            textField.borderStyle = .roundedRect
            stackView.addArrangedSubview(textField)
        }
        
        titleTextField.placeholder = "Title"
        usernameTextField.placeholder = "Username"
        usernameTextField.autocapitalizationType = .none
        passwordTextField.placeholder = "Password"
        passwordTextField.isSecureTextEntry = true
        
        viewPasswordButton.setTitle("View Password", for: .normal)
        viewPasswordButton.addTarget(self, action: #selector(togglePasswordVisibility), for: .touchUpInside)
        viewPasswordButton.isHidden = true
        stackView.addArrangedSubview(viewPasswordButton)
        
        saveButton.setTitle("Save", for: .normal)
        saveButton.addTarget(self, action: #selector(savePassword), for: .touchUpInside)
        stackView.addArrangedSubview(saveButton)
        
        if passwordToEdit != nil {
            title = "Edit Password"
            deleteButton.setTitle("Delete", for: .normal)
            deleteButton.setTitleColor(.red, for: .normal)
            deleteButton.addTarget(self, action: #selector(deletePassword), for: .touchUpInside)
            stackView.addArrangedSubview(deleteButton)
        } else {
            title = "Add Password"
        }
        
        if let password = passwordToEdit {
            titleTextField.text = password.title
            usernameTextField.text = password.username
            passwordTextField.text = password.password
            viewPasswordButton.isHidden = password.password.isEmpty
        }
    }
    
    func setupDelegates() {
        passwordTextField.addTarget(self, action: #selector(passwordTextFieldDidChange), for: .editingChanged)
    }
    
    @objc func passwordTextFieldDidChange() {
        viewPasswordButton.isHidden = passwordTextField.text?.isEmpty ?? true
    }
    
    @objc func togglePasswordVisibility() {
        passwordTextField.isSecureTextEntry.toggle()
        let buttonTitle = passwordTextField.isSecureTextEntry ? "View Password" : "Hide Password"
        viewPasswordButton.setTitle(buttonTitle, for: .normal)
    }
    
//    @objc func savePassword() {
//            guard let title = titleTextField.text, !title.isEmpty,
//                  let username = usernameTextField.text, !username.isEmpty,
//                  let password = passwordTextField.text, !password.isEmpty else {
//                // Show an alert that all fields are required
//                return
//            }
    @objc func savePassword() {
        guard let title = titleTextField.text, !title.isEmpty,
              let username = usernameTextField.text, !username.isEmpty,
              let password = passwordTextField.text, !password.isEmpty else {
            // Show an alert that all fields are required
            return
        }
        
        let notes = notesTextView.text.trimmingCharacters(in: .whitespacesAndNewlines)
        let newPassword = Password(
            id: passwordToEdit?.id ?? UUID().uuidString,  // Changed to use String
            title: title,
            username: username,
            password: password,
            notes: notes.isEmpty ? nil : notes
        )
        
        onSave?(newPassword)
        navigationController?.popViewController(animated: true)
    }   
    
    @objc func deletePassword() {
        let alertController = UIAlertController(title: "Delete Password", message: "Are you sure you want to delete this password?", preferredStyle: .alert)
        
        alertController.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        alertController.addAction(UIAlertAction(title: "Delete", style: .destructive) { _ in
            self.onDelete?()
            self.navigationController?.popViewController(animated: true)
        })
        
        present(alertController, animated: true, completion: nil)
    }
}
*/
