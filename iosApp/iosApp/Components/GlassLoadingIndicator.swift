import SwiftUI

// MARK: - Wavy Shape

struct WavyShape: Shape {
    var phase: CGFloat
    var frequency: CGFloat
    var amplitude: CGFloat

    func path(in rect: CGRect) -> Path {
        var path = Path()
        let width = rect.width
        let height = rect.height
        let midY = rect.midY

        path.move(to: CGPoint(x: 0, y: midY))

        for x in stride(from: 0, to: width, by: 1) {
            let relativeX = x / width
            let y = midY + sin(relativeX * frequency * .pi * 2 + phase) * amplitude
            path.addLine(to: CGPoint(x: x, y: y))
        }

        return path
    }
}

// MARK: - Loading Indicator Variants
// Glass-themed loading animations with Silent Waters colors

enum LoadingVariant {
    case wavy
    case pulsing
    case morphing
    case linear

    var defaultSize: CGFloat {
        switch self {
        case .wavy, .pulsing, .morphing: return 48
        case .linear: return 200
        }
    }
}

// MARK: - Glass Loading Indicator

struct GlassLoadingIndicator: View {
    let variant: LoadingVariant
    let size: CGFloat
    let color: Color

    init(
        variant: LoadingVariant = .pulsing,
        size: CGFloat? = nil,
        color: Color = .coastalPath
    ) {
        self.variant = variant
        self.size = size ?? variant.defaultSize
        self.color = color
    }

    var body: some View {
        Group {
            switch variant {
            case .wavy:
                WavySpinner(size: size, color: color)
            case .pulsing:
                PulsingGlass(size: size, color: color)
            case .morphing:
                MorphingShapes(size: size, color: color)
            case .linear:
                LinearProgress(width: size, color: color)
            }
        }
    }
}

// MARK: - Wavy Spinner

private struct WavySpinner: View {
    let size: CGFloat
    let color: Color

    @State private var rotation: Double = 0
    @State private var phase: CGFloat = 0

    var body: some View {
        ZStack {
            Circle()
                .trim(from: 0, to: 0.7)
                .stroke(
                    LinearGradient(
                        colors: [
                            color.opacity(0.8),
                            color.opacity(0.4),
                            color.opacity(0.1)
                        ],
                        startPoint: .leading,
                        endPoint: .trailing
                    ),
                    style: StrokeStyle(
                        lineWidth: size * 0.1,
                        lineCap: .round
                    )
                )
                .frame(width: size, height: size)
                .rotationEffect(.degrees(rotation))
                .overlay(
                    WavyShape(phase: phase, frequency: 3, amplitude: size * 0.05)
                        .stroke(color.opacity(0.3), lineWidth: 2)
                )
        }
        .onAppear {
            withAnimation(
                .linear(duration: 1.5)
                .repeatForever(autoreverses: false)
            ) {
                rotation = 360
            }
            withAnimation(
                .linear(duration: 2.0)
                .repeatForever(autoreverses: false)
            ) {
                phase = .pi * 2
            }
        }
    }
}

// MARK: - Pulsing Glass

private struct PulsingGlass: View {
    let size: CGFloat
    let color: Color

    @State private var scale1: CGFloat = 0.5
    @State private var scale2: CGFloat = 0.5
    @State private var scale3: CGFloat = 0.5
    @State private var opacity1: Double = 1.0
    @State private var opacity2: Double = 1.0
    @State private var opacity3: Double = 1.0

    var body: some View {
        ZStack {
            // Outer ring
            Circle()
                .fill(
                    RadialGradient(
                        colors: [
                            color.opacity(0.4),
                            color.opacity(0.1)
                        ],
                        center: .center,
                        startRadius: 0,
                        endRadius: size / 2
                    )
                )
                .frame(width: size * scale1, height: size * scale1)
                .opacity(opacity1)
                .glassEffectTinted(.coastalPath, opacity: 0.6)
                .cornerRadius(size)

            // Middle ring
            Circle()
                .fill(
                    RadialGradient(
                        colors: [
                            color.opacity(0.5),
                            color.opacity(0.2)
                        ],
                        center: .center,
                        startRadius: 0,
                        endRadius: size / 3
                    )
                )
                .frame(width: size * 0.7 * scale2, height: size * 0.7 * scale2)
                .opacity(opacity2)
                .glassEffectTinted(.coastalPath, opacity: 0.6)
.cornerRadius(size)

            // Inner ring
            Circle()
                .fill(
                    RadialGradient(
                        colors: [
                            color.opacity(0.8),
                            color.opacity(0.4)
                        ],
                        center: .center,
                        startRadius: 0,
                        endRadius: size / 4
                    )
                )
                .frame(width: size * 0.4 * scale3, height: size * 0.4 * scale3)
                .opacity(opacity3)
                .glassEffectTinted(.coastalPath, opacity: 0.6)
.cornerRadius(size)
        }
        .onAppear {
            withAnimation(
                .easeInOut(duration: 1.5)
                .repeatForever(autoreverses: true)
            ) {
                scale1 = 1.2
                opacity1 = 0.3
            }

            withAnimation(
                .easeInOut(duration: 1.5)
                .repeatForever(autoreverses: true)
                .delay(0.3)
            ) {
                scale2 = 1.3
                opacity2 = 0.2
            }

            withAnimation(
                .easeInOut(duration: 1.5)
                .repeatForever(autoreverses: true)
                .delay(0.6)
            ) {
                scale3 = 1.4
                opacity3 = 0.1
            }
        }
    }
}

// MARK: - Morphing Shapes

private struct MorphingShapes: View {
    let size: CGFloat
    let color: Color

    @State private var currentShape = 0
    @State private var rotation: Double = 0

    var body: some View {
        ZStack {
            Group {
                switch currentShape % 3 {
                case 0:
                    Triangle()
                        .fill(
                            LinearGradient(
                                colors: [
                                    color.opacity(0.6),
                                    color.opacity(0.3)
                                ],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .transition(.opacity)
                case 1:
                    Circle()
                        .fill(
                            RadialGradient(
                                colors: [
                                    color.opacity(0.6),
                                    color.opacity(0.3)
                                ],
                                center: .center,
                                startRadius: 0,
                                endRadius: size / 2
                            )
                        )
                        .transition(.opacity)
                default:
                    Hexagon()
                        .fill(
                            LinearGradient(
                                colors: [
                                    color.opacity(0.6),
                                    color.opacity(0.3)
                                ],
                                startPoint: .top,
                                endPoint: .bottom
                            )
                        )
                        .transition(.opacity)
                }
            }
            .frame(width: size, height: size)
            .glassEffectTinted(.coastalPath, opacity: 0.6)
.cornerRadius(size * 0.1)
            .rotationEffect(.degrees(rotation))
        }
        .onAppear {
            Timer.scheduledTimer(withTimeInterval: 1.2, repeats: true) { _ in
                withAnimation(MotionConfig.shapeMorph) {
                    currentShape += 1
                }
            }

            withAnimation(
                .linear(duration: 3.0)
                .repeatForever(autoreverses: false)
            ) {
                rotation = 360
            }
        }
    }
}

// MARK: - Linear Progress

private struct LinearProgress: View {
    let width: CGFloat
    let color: Color

    @State private var offset: CGFloat = -100

    var body: some View {
        GeometryReader { _ in
            ZStack(alignment: .leading) {
                // Track
                RoundedRectangle(cornerRadius: 4)
                    .fill(color.opacity(0.2))
                    .frame(width: width, height: 8)
                    .glassEffectTinted(.coastalPath, opacity: 0.6)
.cornerRadius(4)

                // Progress indicator
                RoundedRectangle(cornerRadius: 4)
                    .fill(
                        LinearGradient(
                            colors: [
                                color.opacity(0.4),
                                color.opacity(0.8),
                                color.opacity(0.4)
                            ],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .frame(width: width * 0.4, height: 8)
                    .glassEffectTinted(.coastalPath, opacity: 0.6)
.cornerRadius(4)
                    // .shimmer(style: .prominent, isActive: true)
                    .offset(x: offset)
            }
            .frame(width: width)
            .onAppear {
                withAnimation(
                    .linear(duration: 1.5)
                    .repeatForever(autoreverses: false)
                ) {
                    offset = width
                }
            }
        }
        .frame(width: width, height: 8)
    }
}

// MARK: - Triangle Shape

private struct Triangle: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.midX, y: rect.minY))
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.maxY))
        path.addLine(to: CGPoint(x: rect.minX, y: rect.maxY))
        path.closeSubpath()
        return path
    }
}

// MARK: - Hexagon Shape

private struct Hexagon: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        let width = rect.width
        let height = rect.height
        let quarterWidth = width / 4
        let halfHeight = height / 2

        path.move(to: CGPoint(x: quarterWidth, y: 0))
        path.addLine(to: CGPoint(x: width - quarterWidth, y: 0))
        path.addLine(to: CGPoint(x: width, y: halfHeight))
        path.addLine(to: CGPoint(x: width - quarterWidth, y: height))
        path.addLine(to: CGPoint(x: quarterWidth, y: height))
        path.addLine(to: CGPoint(x: 0, y: halfHeight))
        path.closeSubpath()

        return path
    }
}

// MARK: - Previews

#Preview("Wavy Spinner") {
    VStack(spacing: 32) {
        GlassLoadingIndicator(variant: .wavy, size: 48, color: .coastalPath)
        GlassLoadingIndicator(variant: .wavy, size: 72, color: .seaGlass)
    }
    .padding()
    .background(Color.backgroundLight)
}

#Preview("Pulsing Glass") {
    VStack(spacing: 32) {
        GlassLoadingIndicator(variant: .pulsing, size: 48, color: .blueSlate)
        GlassLoadingIndicator(variant: .pulsing, size: 72, color: .coolSteel)
    }
    .padding()
    .background(Color.backgroundLight)
}

#Preview("Morphing Shapes") {
    VStack(spacing: 32) {
        GlassLoadingIndicator(variant: .morphing, size: 48, color: .lightBlue)
        GlassLoadingIndicator(variant: .morphing, size: 72, color: .coastalPath)
    }
    .padding()
    .background(Color.backgroundLight)
}

#Preview("Linear Progress") {
    VStack(spacing: 32) {
        GlassLoadingIndicator(variant: .linear, size: 200, color: .coastalPath)
        GlassLoadingIndicator(variant: .linear, size: 300, color: .seaGlass)
    }
    .padding()
    .background(Color.backgroundLight)
}

#Preview("All Variants") {
    VStack(spacing: 32) {
        Text("Wavy Spinner")
            .font(.caption)
        GlassLoadingIndicator(variant: .wavy, size: 48, color: .coastalPath)

        Text("Pulsing Glass")
            .font(.caption)
        GlassLoadingIndicator(variant: .pulsing, size: 48, color: .blueSlate)

        Text("Morphing Shapes")
            .font(.caption)
        GlassLoadingIndicator(variant: .morphing, size: 48, color: .seaGlass)

        Text("Linear Progress")
            .font(.caption)
        GlassLoadingIndicator(variant: .linear, size: 250, color: .coolSteel)
    }
    .padding()
    .background(Color.backgroundLight)
}
