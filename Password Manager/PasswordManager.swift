import Foundation

class PasswordManager {
    static let shared = PasswordManager()
    
    private init() {}
    
    func getAllPasswords() -> [Password] {
        if let savedPasswordsData = UserDefaults.standard.data(forKey: "savedPasswords"),
           let savedPasswords = try? JSONDecoder().decode([Password].self, from: savedPasswordsData) {
            return savedPasswords
        }
        return []
    }
    
    func savePassword(_ newPassword: Password) {
        var savedPasswords = getAllPasswords()
        savedPasswords.append(newPassword)
        savePasswords(savedPasswords)
    }
    
    func deletePassword(_ password: Password) {
        var savedPasswords = getAllPasswords()
        savedPasswords.removeAll { $0.id == password.id }
        savePasswords(savedPasswords)
    }
    
    func updatePassword(_ updatedPassword: Password) {
        var savedPasswords = getAllPasswords()
        if let index = savedPasswords.firstIndex(where: { $0.id == updatedPassword.id }) {
            savedPasswords[index] = updatedPassword
            savePasswords(savedPasswords)
        }
    }
    
    private func savePasswords(_ passwords: [Password]) {
        if let encodedData = try? JSONEncoder().encode(passwords) {
            UserDefaults.standard.set(encodedData, forKey: "savedPasswords")
        }
    }
    
    func deleteAllPasswords() {
        UserDefaults.standard.removeObject(forKey: "savedPasswords")
        UserDefaults.standard.synchronize()
    }
    
    func replaceAllPasswords(with newPasswords: [Password]) {
            savePasswords(newPasswords)
        }
}
