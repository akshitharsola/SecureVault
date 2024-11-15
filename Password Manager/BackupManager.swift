import Foundation
import CryptoKit

class BackupManager {
    static let shared = BackupManager()
    
    private init() {}
    
    func createBackup(passwords: [Password], withPassword password: String) -> URL? {
        do {
            // Convert passwords to Data
            let passwordsData = try JSONEncoder().encode(passwords)
            
            // Encrypt the data
            let encryptedData = try encrypt(data: passwordsData, using: password)
            
            // Create backup filename with timestamp
            let timestamp = DateFormatter.localizedString(from: Date(), dateStyle: .short, timeStyle: .short)
                .replacingOccurrences(of: "/", with: "-")
                .replacingOccurrences(of: ":", with: "-")
            let filename = "passwords_backup_\(timestamp).enc"
            
            // Get backup file URL
            let backupURL = getDocumentsDirectory().appendingPathComponent(filename)
            
            // Write to file
            try encryptedData.write(to: backupURL)
            
            return backupURL
        } catch {
            print("Error creating backup: \(error)")
            return nil
        }
    }
    
    func restoreBackup(data: Data, withPassword password: String) -> [Password]? {
        do {
            // Decrypt the data
            let decryptedData = try decrypt(data: data, using: password)
            
            // Convert back to passwords
            let passwords = try JSONDecoder().decode([Password].self, from: decryptedData)
            return passwords
        } catch {
            print("Error restoring backup: \(error)")
            return nil
        }
    }
    
    private func encrypt(data: Data, using password: String) throws -> Data {
        let key = SymmetricKey(data: SHA256.hash(data: password.data(using: .utf8)!))
        return try AES.GCM.seal(data, using: key).combined!
    }
    
    private func decrypt(data: Data, using password: String) throws -> Data {
        let key = SymmetricKey(data: SHA256.hash(data: password.data(using: .utf8)!))
        let sealedBox = try AES.GCM.SealedBox(combined: data)
        return try AES.GCM.open(sealedBox, using: key)
    }
    
    private func getDocumentsDirectory() -> URL {
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
    }
}
