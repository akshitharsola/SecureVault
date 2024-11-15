import Foundation

struct Password: Codable, Identifiable {
    var id: String
    var title: String
    var username: String
    var password: String
    var notes: String?
    
    enum CodingKeys: String, CodingKey {
        case id, title, username, password, notes
    }
    
    init(id: String = UUID().uuidString, title: String, username: String, password: String, notes: String? = nil) {
        self.id = id
        self.title = title
        self.username = username
        self.password = password
        self.notes = notes
    }
    
    // Updated method to handle clipboard formatting better
    func getCombinedCredentials() -> String {
        // Using a newline character instead of tab for better paste behavior
        return "\(username)\n\(password)"
    }
}

//import Foundation
//struct Password: Codable, Identifiable {
//    let id: UUID
//    var title: String
//    var username: String
//    var password: String
//    // Add any other relevant properties
//    
//    init(id: UUID = UUID(), title: String, username: String, password: String) {
//        self.id = id
//        self.title = title
//        self.username = username
//        self.password = password
//    }
//}
