import UIKit

class ThemeManager {
    static let shared = ThemeManager()
    
    private init() {}
    
    // MARK: - Theme Keys
    private enum Keys {
        static let currentTheme = "AppTheme"
        static let lastUsedTheme = "LastUsedTheme"
    }
    
    // MARK: - Default Themes
    static let defaultLightTheme = AppTheme(
        backgroundColor: .systemBackground,
        boxBackgroundColor: .secondarySystemBackground,
        textColor: .label,
        accentColor: .systemBlue
    )
    
    static let defaultDarkTheme = AppTheme(
        backgroundColor: .systemBackground,
        boxBackgroundColor: .secondarySystemBackground,
        textColor: .label,
        accentColor: .systemBlue
    )
    
    // MARK: - Theme Management
    static func saveTheme(_ theme: AppTheme) {
        if let encoded = try? JSONEncoder().encode(theme) {
            UserDefaults.standard.set(encoded, forKey: Keys.currentTheme)
            UserDefaults.standard.synchronize()
            
            // Post notification for theme change
            NotificationCenter.default.post(
                name: .themeDidChange,
                object: nil,
                userInfo: ["theme": theme]
            )
        }
    }
    
    static func loadTheme() -> AppTheme {
        if let savedTheme = UserDefaults.standard.object(forKey: Keys.currentTheme) as? Data,
           let loadedTheme = try? JSONDecoder().decode(AppTheme.self, from: savedTheme) {
            return loadedTheme
        }
        return defaultLightTheme
    }
    
    // MARK: - System Theme Integration
    static func updateForSystemTheme() {
        let isDarkMode = UITraitCollection.current.userInterfaceStyle == .dark
        let theme = isDarkMode ? defaultDarkTheme : defaultLightTheme
        saveTheme(theme)
    }
}

// MARK: - AppTheme Definition
struct AppTheme: Codable {
    var backgroundColor: UIColor
    var boxBackgroundColor: UIColor
    var textColor: UIColor
    var accentColor: UIColor
    
    enum CodingKeys: String, CodingKey {
        case backgroundColor, boxBackgroundColor, textColor, accentColor
    }
    
    // MARK: - Encoding
    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(backgroundColor.toHexString(), forKey: .backgroundColor)
        try container.encode(boxBackgroundColor.toHexString(), forKey: .boxBackgroundColor)
        try container.encode(textColor.toHexString(), forKey: .textColor)
        try container.encode(accentColor.toHexString(), forKey: .accentColor)
    }
    
    // MARK: - Decoding
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        backgroundColor = UIColor(hexString: try container.decode(String.self, forKey: .backgroundColor)) ?? .systemBackground
        boxBackgroundColor = UIColor(hexString: try container.decode(String.self, forKey: .boxBackgroundColor)) ?? .secondarySystemBackground
        textColor = UIColor(hexString: try container.decode(String.self, forKey: .textColor)) ?? .label
        accentColor = UIColor(hexString: try container.decode(String.self, forKey: .accentColor)) ?? .systemBlue
    }
    
    // MARK: - Initialization
    init(backgroundColor: UIColor, boxBackgroundColor: UIColor, textColor: UIColor, accentColor: UIColor) {
        self.backgroundColor = backgroundColor
        self.boxBackgroundColor = boxBackgroundColor
        self.textColor = textColor
        self.accentColor = accentColor
    }
    
    // MARK: - Theme Presets
    static var systemLight: AppTheme {
        AppTheme(
            backgroundColor: .systemBackground,
            boxBackgroundColor: .secondarySystemBackground,
            textColor: .label,
            accentColor: .systemBlue
        )
    }
    
    static var systemDark: AppTheme {
        AppTheme(
            backgroundColor: .systemBackground,
            boxBackgroundColor: .secondarySystemBackground,
            textColor: .label,
            accentColor: .systemBlue
        )
    }
    
    static var sepia: AppTheme {
        AppTheme(
            backgroundColor: UIColor(red: 0.98, green: 0.95, blue: 0.90, alpha: 1.0),
            boxBackgroundColor: UIColor(red: 0.95, green: 0.92, blue: 0.85, alpha: 1.0),
            textColor: UIColor(red: 0.25, green: 0.22, blue: 0.18, alpha: 1.0),
            accentColor: UIColor(red: 0.60, green: 0.35, blue: 0.15, alpha: 1.0)
        )
    }
}

// MARK: - UIColor Extension
extension UIColor {
    func toHexString() -> String {
        var r: CGFloat = 0
        var g: CGFloat = 0
        var b: CGFloat = 0
        var a: CGFloat = 0
        
        getRed(&r, green: &g, blue: &b, alpha: &a)
        
        let rgb: Int = (Int)(r * 255) << 16 | (Int)(g * 255) << 8 | (Int)(b * 255) << 0
        
        return String(format: "#%06x", rgb)
    }
    
    convenience init?(hexString: String) {
        let r, g, b, a: CGFloat
        
        if hexString.hasPrefix("#") {
            let start = hexString.index(hexString.startIndex, offsetBy: 1)
            let hexColor = String(hexString[start...])
            
            if hexColor.count == 6 {
                let scanner = Scanner(string: hexColor)
                var hexNumber: UInt64 = 0
                
                if scanner.scanHexInt64(&hexNumber) {
                    r = CGFloat((hexNumber & 0xff0000) >> 16) / 255
                    g = CGFloat((hexNumber & 0x00ff00) >> 8) / 255
                    b = CGFloat(hexNumber & 0x0000ff) / 255
                    a = 1.0
                    
                    self.init(red: r, green: g, blue: b, alpha: a)
                    return
                }
            }
        }
        
        return nil
    }
}

// MARK: - Theme Observer Protocol
protocol ThemeObserver: AnyObject {
    func themeDidChange(_ theme: AppTheme)
}
