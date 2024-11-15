import UIKit

class SettingsViewController: UITableViewController {
    
    weak var delegate: ViewControllerDelegate?
    @objc private func backupButtonTapped() {
            delegate?.backupPasswords()
        }
        
    @objc private func restoreButtonTapped() {
            delegate?.restorePasswords()
        }
    
    enum ThemeElement: String, CaseIterable {
            case backgroundColor = "Background Color"
            case boxBackgroundColor = "Box Background Color"
            case textColor = "Text Color"
            case accentColor = "Accent Color"
        }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "Settings"
        tableView.register(UITableViewCell.self, forCellReuseIdentifier: "SettingsCell")
    }
    
    
    override func numberOfSections(in tableView: UITableView) -> Int {
        return 2
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return section == 0 ? ThemeElement.allCases.count : 2
    }
    
    override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return section == 0 ? "Theme Colors" : "Password Management"
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "SettingsCell", for: indexPath)
        
        if indexPath.section == 0 {
            let element = ThemeElement.allCases[indexPath.row]
            cell.textLabel?.text = element.rawValue
            cell.accessoryType = .disclosureIndicator
        } else {
            cell.textLabel?.text = indexPath.row == 0 ? "Backup Passwords" : "Restore Passwords"
        }
        
        return cell
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        
        if indexPath.section == 0 {
            let element = ThemeElement.allCases[indexPath.row]
            showColorPicker(for: element)
        } else if indexPath.row == 0 {
            delegate?.backupPasswords()
        } else {
            delegate?.restorePasswords()
        }
    }
    
    func showColorPicker(for element: ThemeElement) {
        let colorPicker = UIColorPickerViewController()
        colorPicker.selectedColor = getCurrentColor(for: element)
        colorPicker.delegate = self
        colorPicker.title = element.rawValue
        colorPicker.supportsAlpha = false
        present(colorPicker, animated: true)
    }
    
    func getCurrentColor(for element: ThemeElement) -> UIColor {
        let currentTheme = ThemeManager.loadTheme()
        switch element {
        case .backgroundColor:
            return currentTheme.backgroundColor
        case .boxBackgroundColor:
            return currentTheme.boxBackgroundColor
        case .textColor:
            return currentTheme.textColor
        case .accentColor:
            return currentTheme.accentColor
        }
    }
}

extension SettingsViewController: UIColorPickerViewControllerDelegate {
    func colorPickerViewControllerDidFinish(_ viewController: UIColorPickerViewController) {
        updateTheme(with: viewController.selectedColor, for: viewController.title)
        viewController.dismiss(animated: true) { [weak self] in
            self?.delegate?.applyTheme()
        }
    }
    
    func colorPickerViewControllerDidSelectColor(_ viewController: UIColorPickerViewController) {
        updateTheme(with: viewController.selectedColor, for: viewController.title)
        delegate?.applyTheme()
    }
    
    private func updateTheme(with color: UIColor, for elementTitle: String?) {
            guard let title = elementTitle, let element = ThemeElement(rawValue: title) else { return }
            
            var currentTheme = ThemeManager.loadTheme()
            switch element {
            case .backgroundColor:
                currentTheme.backgroundColor = color
            case .boxBackgroundColor:
                currentTheme.boxBackgroundColor = color
            case .textColor:
                currentTheme.textColor = color
            case .accentColor:
                currentTheme.accentColor = color
            }
            
            ThemeManager.saveTheme(currentTheme)
            delegate?.applyTheme()
        }
}



//import UIKit
//
////protocol ViewControllerDelegate: AnyObject {
////    func applyTheme()
////    func backupPasswords()
////    func restorePasswords()
////}
//
//class SettingsViewController: UITableViewController {
//    
//    weak var delegate: ViewControllerDelegate?
//        
//        enum ThemeElement: String, CaseIterable {
//            case backgroundColor = "Background Color"
//            case boxBackgroundColor = "Box Background Color"
//            case accentColor = "Accent Color"
//        }
//    
//    override func viewDidLoad() {
//        super.viewDidLoad()
//        
//        title = "Settings"
//        tableView.register(UITableViewCell.self, forCellReuseIdentifier: "SettingsCell")
//    }
//    
//    override func numberOfSections(in tableView: UITableView) -> Int {
//        return 2
//    }
//    
//    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
//        return section == 0 ? ThemeElement.allCases.count : 2
//    }
//    
//    override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
//        return section == 0 ? "Theme Colors" : "Password Management"
//    }
//    
//    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
//        let cell = tableView.dequeueReusableCell(withIdentifier: "SettingsCell", for: indexPath)
//        
//        if indexPath.section == 0 {
//            let element = ThemeElement.allCases[indexPath.row]
//            cell.textLabel?.text = element.rawValue
//            cell.accessoryType = .disclosureIndicator
//        } else {
//            cell.textLabel?.text = indexPath.row == 0 ? "Backup Passwords" : "Restore Passwords"
//        }
//        
//        return cell
//    }
//    
//    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
//        tableView.deselectRow(at: indexPath, animated: true)
//        
//        if indexPath.section == 0 {
//            let element = ThemeElement.allCases[indexPath.row]
//            showColorPicker(for: element)
//        } else if indexPath.row == 0 {
//            delegate?.backupPasswords()
//        } else {
//            delegate?.restorePasswords()
//        }
//    }
//    
//    func showColorPicker(for element: ThemeElement) {
//            let colorPicker = UIColorPickerViewController()
//            colorPicker.selectedColor = getCurrentColor(for: element)
//            colorPicker.delegate = self
//            colorPicker.title = element.rawValue
//            colorPicker.supportsAlpha = false
//            present(colorPicker, animated: true)
//        }
//    
//    func getCurrentColor(for element: ThemeElement) -> UIColor {
//            let currentTheme = ThemeManager.loadTheme()
//            switch element {
//            case .backgroundColor:
//                return currentTheme.backgroundColor
//            case .boxBackgroundColor:
//                return currentTheme.boxBackgroundColor
//            case .accentColor:
//                return currentTheme.accentColor
//            }
//        }
//}
//
//extension SettingsViewController: UIColorPickerViewControllerDelegate {
//    func colorPickerViewControllerDidFinish(_ viewController: UIColorPickerViewController) {
//        updateTheme(with: viewController.selectedColor, for: viewController.title)
//        viewController.dismiss(animated: true) {
//            self.delegate?.applyTheme()
//        }
//    }
//    
//    func colorPickerViewControllerDidSelectColor(_ viewController: UIColorPickerViewController) {
//        updateTheme(with: viewController.selectedColor, for: viewController.title)
//    }
//    
//    private func updateTheme(with color: UIColor, for elementTitle: String?) {
//        guard let title = elementTitle, let element = ThemeElement(rawValue: title) else { return }
//        
//        var currentTheme = ThemeManager.loadTheme()
//        switch element {
//        case .backgroundColor:
//            currentTheme.backgroundColor = color
//        case .boxBackgroundColor:
//            currentTheme.boxBackgroundColor = color
//        case .accentColor:
//            currentTheme.accentColor = color
//        }
//        
//        ThemeManager.saveTheme(currentTheme)
//    }
//}
//
//class SettingsViewController: UITableViewController {
//    
//    weak var delegate: ViewControllerDelegate?
//    
//    enum ThemeElement: String, CaseIterable {
//        case backgroundColor = "Background Color"
//        case boxBackgroundColor = "Box Background Color"
//        case accentColor = "Accent Color"
//    }
//    
//    override func viewDidLoad() {
//        super.viewDidLoad()
//        
//        title = "Settings"
//        tableView.register(UITableViewCell.self, forCellReuseIdentifier: "SettingsCell")
//    }
//    
//    override func numberOfSections(in tableView: UITableView) -> Int {
//        return 2
//    }
//    
//    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
//        return section == 0 ? ThemeElement.allCases.count : 2
//    }
//    
//    override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
//        return section == 0 ? "Theme Colors" : "Password Management"
//    }
//    
//    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
//        let cell = tableView.dequeueReusableCell(withIdentifier: "SettingsCell", for: indexPath)
//        
//        if indexPath.section == 0 {
//            let element = ThemeElement.allCases[indexPath.row]
//            cell.textLabel?.text = element.rawValue
//            cell.accessoryType = .disclosureIndicator
//        } else {
//            cell.textLabel?.text = indexPath.row == 0 ? "Backup Passwords" : "Restore Passwords"
//        }
//        
//        return cell
//    }
//    
//    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
//        tableView.deselectRow(at: indexPath, animated: true)
//        
//        if indexPath.section == 0 {
//            let element = ThemeElement.allCases[indexPath.row]
//            showColorPicker(for: element)
//        } else if indexPath.row == 0 {
//            delegate?.backupPasswords()
//        } else {
//            delegate?.restorePasswords()
//        }
//    }
//    
//    func showColorPicker(for element: ThemeElement) {
//        let colorPicker = UIColorPickerViewController()
//        colorPicker.selectedColor = getCurrentColor(for: element)
//        colorPicker.delegate = self
//        colorPicker.title = element.rawValue
//        colorPicker.supportsAlpha = false
//        present(colorPicker, animated: true)
//    }
//    
//    func getCurrentColor(for element: ThemeElement) -> UIColor {
//        let currentTheme = ThemeManager.loadTheme()
//        switch element {
//        case .backgroundColor:
//            return currentTheme.backgroundColor
//        case .boxBackgroundColor:
//            return currentTheme.boxBackgroundColor
//        case .accentColor:
//            return currentTheme.accentColor
//        }
//    }
//}
//
//extension SettingsViewController: UIColorPickerViewControllerDelegate {
//    func colorPickerViewControllerDidFinish(_ viewController: UIColorPickerViewController) {
//        updateTheme(with: viewController.selectedColor, for: viewController.title)
//        viewController.dismiss(animated: true) {
//            self.delegate?.applyTheme()
//        }
//    }
//    
//    func colorPickerViewControllerDidSelectColor(_ viewController: UIColorPickerViewController) {
//        updateTheme(with: viewController.selectedColor, for: viewController.title)
//    }
//    
//    private func updateTheme(with color: UIColor, for elementTitle: String?) {
//        guard let title = elementTitle, let element = ThemeElement(rawValue: title) else { return }
//        
//        var currentTheme = ThemeManager.loadTheme()
//        switch element {
//        case .backgroundColor:
//            currentTheme.backgroundColor = color
//        case .boxBackgroundColor:
//            currentTheme.boxBackgroundColor = color
//        case .accentColor:
//            currentTheme.accentColor = color
//        }
//        
//        ThemeManager.saveTheme(currentTheme)
//    }
//}
//
//
////import UIKit
////protocol ViewControllerDelegate: AnyObject {
////    func applyTheme()
////    func backupPasswords()
////    func restorePasswords()
////}
////
////class SettingsViewController: UITableViewController {
////    
////    weak var delegate: ViewControllerDelegate?
////        
////        enum ThemeElement: String, CaseIterable {
////            case backgroundColor = "Background Color"
////            case boxBackgroundColor = "Box Background Color"
////            case accentColor = "Accent Color"
////        }
////    
////    override func viewDidLoad() {
////        super.viewDidLoad()
////        
////        title = "Settings"
////        tableView.register(UITableViewCell.self, forCellReuseIdentifier: "SettingsCell")
////    }
////    
////    override func numberOfSections(in tableView: UITableView) -> Int {
////        return 2
////    }
////    
////    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
////        return section == 0 ? ThemeElement.allCases.count : 2
////    }
////    
////    override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
////        return section == 0 ? "Theme Colors" : "Password Management"
////    }
////    
////    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
////        let cell = tableView.dequeueReusableCell(withIdentifier: "SettingsCell", for: indexPath)
////        
////        if indexPath.section == 0 {
////            let element = ThemeElement.allCases[indexPath.row]
////            cell.textLabel?.text = element.rawValue
////            cell.accessoryType = .disclosureIndicator
////        } else {
////            cell.textLabel?.text = indexPath.row == 0 ? "Backup Passwords" : "Restore Passwords"
////        }
////        
////        return cell
////    }
////    
////    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
////        tableView.deselectRow(at: indexPath, animated: true)
////        
////        if indexPath.section == 0 {
////            let element = ThemeElement.allCases[indexPath.row]
////            showColorPicker(for: element)
////        } else if indexPath.row == 0 {
////            delegate?.backupPasswords()
////        } else {
////            delegate?.restorePasswords()
////        }
////    }
////    
////    func showColorPicker(for element: ThemeElement) {
////        let colorPicker = UIColorPickerViewController()
////        colorPicker.selectedColor = getCurrentColor(for: element)
////        colorPicker.delegate = self
////        colorPicker.title = element.rawValue
////        colorPicker.supportsAlpha = false
////        present(colorPicker, animated: true)
////    }
////    
////    func getCurrentColor(for element: ThemeElement) -> UIColor {
////        let currentTheme = ThemeManager.loadTheme()
////        switch element {
////        case .backgroundColor:
////            return currentTheme.backgroundColor
////        case .boxBackgroundColor:
////            return currentTheme.boxBackgroundColor
////        case .accentColor:
////            return currentTheme.accentColor
////        }
////    }
////}
////
////extension SettingsViewController: UIColorPickerViewControllerDelegate {
////    func colorPickerViewControllerDidFinish(_ viewController: UIColorPickerViewController) {
////        guard let title = viewController.title, let element = ThemeElement(rawValue: title) else { return }
////        
////        var currentTheme = ThemeManager.loadTheme()
////        switch element {
////        case .backgroundColor:
////            currentTheme.backgroundColor = viewController.selectedColor
////        case .boxBackgroundColor:
////            currentTheme.boxBackgroundColor = viewController.selectedColor
////        case .accentColor:
////            currentTheme.accentColor = viewController.selectedColor
////        }
////        
////        ThemeManager.saveTheme(currentTheme)
////        delegate?.applyTheme()
////    }
////}   
