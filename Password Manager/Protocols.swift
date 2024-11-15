// In Protocols.swift or at the top of your ViewController.swift
import UIKit

protocol ViewControllerDelegate: AnyObject {
    func applyTheme()
    func backupPasswords()
    func restorePasswords()
}
