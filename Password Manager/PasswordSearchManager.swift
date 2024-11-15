import Foundation

class PasswordSearchManager {
    static let shared = PasswordSearchManager()
    
    private init() {}
    
    func searchPasswords(_ passwords: [Password], with searchText: String) -> [Password] {
        guard !searchText.isEmpty else { return passwords }
        
        let lowercasedSearchText = searchText.lowercased()
        return passwords.filter { password in
            password.title.lowercased().contains(lowercasedSearchText) ||
            password.username.lowercased().contains(lowercasedSearchText)
        }
    }
}
