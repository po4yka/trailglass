import SwiftUI
import Shared

// MARK: - Transport Helpers

func transportName(_ type: TransportType) -> String {
    type.name.lowercased().capitalized
}

func transportIcon(_ type: TransportType) -> String {
    switch type.name {
    case "WALK": return "figure.walk"
    case "BIKE": return "bicycle"
    case "CAR": return "car"
    case "TRAIN": return "tram"
    case "PLANE": return "airplane"
    case "BOAT": return "ferry"
    default: return "questionmark.circle"
    }
}

func transportColor(_ type: TransportType) -> Color {
    switch type.name {
    case "WALK": return Color.green // Adaptive colors not available globally yet, using standard
    case "BIKE": return Color.blue
    case "CAR": return Color.orange
    case "TRAIN": return Color.purple
    case "PLANE": return Color.cyan
    case "BOAT": return Color.blue
    default: return Color.gray
    }
}

// MARK: - Category Helpers

func categoryName(_ category: PlaceCategory) -> String {
    switch category {
    case .home: return "Home"
    case .work: return "Work"
    // case .food: return "Food" // Commented out - enum case not available
    // case .shopping: return "Shopping"
    // case .fitness: return "Fitness"
    // case .entertainment: return "Entertainment"
    // case .travel: return "Travel"
    // case .healthcare: return "Healthcare"
    // case .education: return "Education"
    // case .religious: return "Religious"
    // case .social: return "Social"
    // case .outdoor: return "Outdoor"
    // case .service: return "Service"
    // case .other: return "Other"
    default: return "Unknown"
    }
}

func categoryIcon(_ category: PlaceCategory) -> String {
    switch category {
    case .home: return "house"
    case .work: return "briefcase"
    // case .food: return "fork.knife"
    // case .shopping: return "cart"
    // case .fitness: return "figure.run"
    // case .entertainment: return "film"
    // case .travel: return "airplane"
    // case .healthcare: return "cross.case"
    // case .education: return "book"
    // case .religious: return "building.columns"
    // case .social: return "person.2"
    // case .outdoor: return "tree"
    // case .service: return "hammer"
    // case .other: return "mappin"
    default: return "mappin"
    }
}

func categoryColor(_ category: PlaceCategory) -> Color {
    switch category {
    case .home: return Color.green
    case .work: return Color.blue
    // case .food: return Color.orange
    // case .shopping: return Color.yellow
    // case .fitness: return Color.red
    // case .entertainment: return Color.purple
    // case .travel: return Color.cyan
    // case .healthcare: return Color.red
    // case .education: return Color.blue
    // case .religious: return Color.brown
    // case .social: return Color.pink
    // case .outdoor: return Color.green
    // case .service: return Color.gray
    // case .other: return Color.gray
    default: return Color.gray
    }
}

// MARK: - Date & Time Helpers

func dayOfWeekName(_ day: DayOfWeek) -> String {
    switch day.name {
    case "MONDAY": return "Mon"
    case "TUESDAY": return "Tue"
    case "WEDNESDAY": return "Wed"
    case "THURSDAY": return "Thu"
    case "FRIDAY": return "Fri"
    case "SATURDAY": return "Sat"
    case "SUNDAY": return "Sun"
    default: return day.name
    }
}

func timeRangeForHour(_ hour: Int32) -> String {
    switch hour {
    case 0..<6: return "Night (12AM-6AM)"
    case 6..<12: return "Morning (6AM-12PM)"
    case 12..<18: return "Afternoon (12PM-6PM)"
    default: return "Evening (6PM-12AM)"
    }
}

func formatDuration(_ duration: KotlinDuration) -> String {
    let hours = duration.inWholeHours
    let minutes = duration.inWholeMinutes % 60

    if hours > 0 && minutes > 0 {
        return "\(hours)h \(minutes)m"
    } else if hours > 0 {
        return "\(hours)h"
    } else {
        return "\(minutes)m"
    }
}

func formatLocalDate(_ date: LocalDate) -> String {
    "\(date.year)-\(String(format: "%02d", date.monthNumber))-\(String(format: "%02d", date.dayOfMonth))"
}
