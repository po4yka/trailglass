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
    category.name.lowercased().capitalized
}

func categoryIcon(_ category: PlaceCategory) -> String {
    switch category.name {
    case "HOME": return "house"
    case "WORK": return "briefcase"
    case "FOOD": return "fork.knife"
    case "SHOPPING": return "cart"
    case "FITNESS": return "figure.run"
    case "ENTERTAINMENT": return "film"
    case "TRAVEL": return "airplane"
    case "HEALTHCARE": return "cross.case"
    case "EDUCATION": return "book"
    case "RELIGIOUS": return "building.columns"
    case "SOCIAL": return "person.2"
    case "OUTDOOR": return "tree"
    case "SERVICE": return "hammer"
    default: return "mappin"
    }
}

func categoryColor(_ category: PlaceCategory) -> Color {
    switch category.name {
    case "HOME": return Color.green
    case "WORK": return Color.blue
    case "FOOD": return Color.orange
    case "SHOPPING": return Color.yellow
    case "FITNESS": return Color.red
    case "ENTERTAINMENT": return Color.purple
    case "TRAVEL": return Color.cyan
    case "HEALTHCARE": return Color.red
    case "EDUCATION": return Color.blue
    case "RELIGIOUS": return Color.gray
    case "SOCIAL": return Color.pink
    case "OUTDOOR": return Color.green
    case "SERVICE": return Color.gray
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
