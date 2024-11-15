import UIKit

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?
    
    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        guard let windowScene = (scene as? UIWindowScene) else { return }
        
        let window = UIWindow(windowScene: windowScene)
        self.window = window
        
        // Go directly to main app
        let mainVC = ViewController()
        let navController = UINavigationController(rootViewController: mainVC)
        window.rootViewController = navController
        
        window.makeKeyAndVisible()
    }
    
    func sceneDidDisconnect(_ scene: UIScene) {
        // Called as the scene is being released by the system
    }
    
    func sceneDidBecomeActive(_ scene: UIScene) {
        // Called when the scene has moved from an inactive state to an active state
    }
    
    func sceneWillResignActive(_ scene: UIScene) {
        // Called when the scene will move from an active state to an inactive state
    }
    
    func sceneWillEnterForeground(_ scene: UIScene) {
        // Called as the scene transitions from the background to the foreground
    }
    
    func sceneDidEnterBackground(_ scene: UIScene) {
        // Called as the scene transitions from the foreground to the background
    }
}


//
//class LaunchAnimationController: UIViewController {
//    private let logoImageView: UIImageView = {
//        let imageView = UIImageView(image: UIImage(named: "AppIcon")) // Use your logo image name
//        imageView.contentMode = .scaleAspectFit
//        imageView.translatesAutoresizingMaskIntoConstraints = false
//        return imageView
//    }()
//    
//    override func viewDidLoad() {
//        super.viewDidLoad()
//        setupUI()
//    }
//    
//    private func setupUI() {
//        view.backgroundColor = .systemBackground
//        view.addSubview(logoImageView)
//        
//        NSLayoutConstraint.activate([
//            logoImageView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
//            logoImageView.centerYAnchor.constraint(equalTo: view.centerYAnchor),
//            logoImageView.widthAnchor.constraint(equalTo: view.widthAnchor, multiplier: 0.4),
//            logoImageView.heightAnchor.constraint(equalTo: logoImageView.widthAnchor)
//        ])
//    }
//    
//    func startAnimation(completion: @escaping () -> Void) {
//        // Initial state
//        logoImageView.transform = CGAffineTransform(scaleX: 0.8, y: 0.8)
//        logoImageView.alpha = 0
//        
//        // Animate logo appearance
//        UIView.animate(withDuration: 0.9, delay: 0.2, options: .curveEaseOut) {
//            self.logoImageView.transform = .identity
//            self.logoImageView.alpha = 1
//        }
//        
//        // Animate logo fade out with scale
//        UIView.animate(withDuration: 1.0, delay: 1.2, options: .curveEaseIn) {
//            self.logoImageView.transform = CGAffineTransform(scaleX: 1.2, y: 1.2)
//            self.logoImageView.alpha = 0
//            self.view.alpha = 0
//        } completion: { _ in
//            completion()
//        }
//    }
//}
//
//class SceneDelegate: UIResponder, UIWindowSceneDelegate {
//    var window: UIWindow?
//
//    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
//        guard let windowScene = (scene as? UIWindowScene) else { return }
//        
//        let window = UIWindow(windowScene: windowScene)
//        self.window = window
//        
//        // Create and show launch animation
//        let launchVC = LaunchAnimationController()
//        window.rootViewController = launchVC
//        window.makeKeyAndVisible()
//        
//        // Start animation sequence
//        launchVC.startAnimation {
//            self.transitionToMainApp(window: window)
//        }
//    }
//    
//    private func transitionToMainApp(window: UIWindow) {
//        let mainVC = ViewController()
//        let navController = UINavigationController(rootViewController: mainVC)
//        
//        UIView.transition(with: window,
//                         duration: 0.3,
//                         options: .transitionCrossDissolve,
//                         animations: {
//            window.rootViewController = navController
//        })
//    }
//
//    func sceneDidDisconnect(_ scene: UIScene) {
//        // Called as the scene is being released by the system
//    }
//
//    func sceneDidBecomeActive(_ scene: UIScene) {
//        // Called when the scene has moved from an inactive state to an active state
//    }
//
//    func sceneWillResignActive(_ scene: UIScene) {
//        // Called when the scene will move from an active state to an inactive state
//    }
//
//    func sceneWillEnterForeground(_ scene: UIScene) {
//        // Called as the scene transitions from the background to the foreground
//    }
//
//    func sceneDidEnterBackground(_ scene: UIScene) {
//        // Called as the scene transitions from the foreground to the background
//    }
//}

