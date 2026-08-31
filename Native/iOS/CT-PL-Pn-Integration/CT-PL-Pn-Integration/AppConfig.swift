import Foundation

/// Runtime config loaded from the gitignored `secrets.properties` file bundled
/// into the app (and the NotificationServiceExtension). Mirrors the Android
/// `secrets.properties` + BuildConfig setup.
///
/// If the file is missing or a key is absent the value is `""` so the build and
/// launch never crash - only pushes/logs will misbehave until real values are
/// filled in.
enum AppConfig {

    static let clevertapAccountID: String = value(for: "CLEVERTAP_ACCOUNT_ID")
    static let clevertapToken: String = value(for: "CLEVERTAP_TOKEN")
    static let clevertapRegion: String = value(for: "CLEVERTAP_REGION")

    static let plotlineAPIKey: String = value(for: "PLOTLINE_API_KEY")
    static let plotlineUserID: String = value(for: "PLOTLINE_USER_ID")

    static let clevertapUserName: String = value(for: "CLEVERTAP_USER_NAME")
    static let clevertapUserIdentity: String = value(for: "CLEVERTAP_USER_IDENTITY")
    static let clevertapUserEmail: String = value(for: "CLEVERTAP_USER_EMAIL")

    /// Shared App Group used by the app and the NotificationServiceExtension
    /// (Plotline rich push + CleverTap rich media).
    static let appGroupID: String = value(for: "APP_GROUP_ID")

    private static func value(for key: String) -> String {
        guard let url = Bundle.main.url(forResource: "secrets", withExtension: "properties"),
              let contents = try? String(contentsOf: url, encoding: .utf8) else {
            return ""
        }

        for rawLine in contents.components(separatedBy: .newlines) {
            let line = rawLine.trimmingCharacters(in: .whitespaces)
            guard !line.isEmpty, !line.hasPrefix("#") else { continue }
            let parts = line.split(separator: "=", maxSplits: 1)
            if parts.count == 2, parts[0].trimmingCharacters(in: .whitespaces) == key {
                return parts[1].trimmingCharacters(in: .whitespaces)
            }
        }
        return ""
    }
}