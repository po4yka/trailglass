import SwiftUI

// MARK: - Glass Shape Types

enum GlassShape {
    case circle
    case triangle
    case hexagon
    case roundedSquare
    case wave
    case petal

    var pathData: (CGRect) -> Path {
        switch self {
        case .circle:
            return { rect in
                Path(ellipseIn: rect)
            }
        case .triangle:
            return { rect in
                var path = Path()
                path.move(to: CGPoint(x: rect.midX, y: rect.minY))
                path.addLine(to: CGPoint(x: rect.maxX, y: rect.maxY))
                path.addLine(to: CGPoint(x: rect.minX, y: rect.maxY))
                path.closeSubpath()
                return path
            }
        case .hexagon:
            return { rect in
                var path = Path()
                let width = rect.width
                let height = rect.height
                let centerX = rect.midX
                let centerY = rect.midY
                let radius = min(width, height) / 2

                for i in 0..<6 {
                    let angle = CGFloat(i) * .pi / 3
                    let x = centerX + radius * cos(angle)
                    let y = centerY + radius * sin(angle)

                    if i == 0 {
                        path.move(to: CGPoint(x: x, y: y))
                    } else {
                        path.addLine(to: CGPoint(x: x, y: y))
                    }
                }
                path.closeSubpath()
                return path
            }
        case .roundedSquare:
            return { rect in
                Path(roundedRect: rect, cornerRadius: min(rect.width, rect.height) * 0.25)
            }
        case .wave:
            return { rect in
                var path = Path()
                let width = rect.width
                let height = rect.height

                path.move(to: CGPoint(x: rect.minX, y: rect.midY))

                // Create wave pattern
                let waveHeight = height * 0.3
                let waveCount = 2

                for i in 0...waveCount {
                    let x = rect.minX + (width / CGFloat(waveCount)) * CGFloat(i)
                    let isUp = i % 2 == 0

                    if i == 0 {
                        path.move(to: CGPoint(x: x, y: rect.midY))
                    } else {
                        let controlX = x - (width / CGFloat(waveCount)) / 2
                        let controlY = isUp ? rect.midY - waveHeight : rect.midY + waveHeight
                        path.addQuadCurve(
                            to: CGPoint(x: x, y: rect.midY),
                            control: CGPoint(x: controlX, y: controlY)
                        )
                    }
                }

                path.addLine(to: CGPoint(x: rect.maxX, y: rect.maxY))
                path.addLine(to: CGPoint(x: rect.minX, y: rect.maxY))
                path.closeSubpath()
                return path
            }
        case .petal:
            return { rect in
                var path = Path()
                let centerX = rect.midX
                let centerY = rect.midY
                let radius = min(rect.width, rect.height) / 2

                for i in 0..<5 {
                    let angle = CGFloat(i) * 2 * .pi / 5 - .pi / 2
                    let nextAngle = CGFloat(i + 1) * 2 * .pi / 5 - .pi / 2

                    let x = centerX + radius * cos(angle)
                    let y = centerY + radius * sin(angle)

                    if i == 0 {
                        path.move(to: CGPoint(x: x, y: y))
                    }

                    let controlRadius = radius * 0.4
                    let controlAngle = angle + .pi / 5
                    let controlX = centerX + controlRadius * cos(controlAngle)
                    let controlY = centerY + controlRadius * sin(controlAngle)

                    let nextX = centerX + radius * cos(nextAngle)
                    let nextY = centerY + radius * sin(nextAngle)

                    path.addQuadCurve(
                        to: CGPoint(x: nextX, y: nextY),
                        control: CGPoint(x: controlX, y: controlY)
                    )
                }
                path.closeSubpath()
                return path
            }
        }
    }
}

// MARK: - Morphing Glass Shape

struct MorphingGlassShape: Shape {
    var progress: Double
    let startShape: GlassShape
    let endShape: GlassShape

    var animatableData: Double {
        get { progress }
        set { progress = newValue }
    }

    func path(in rect: CGRect) -> Path {
        if progress <= 0 {
            return startShape.pathData(rect)
        } else if progress >= 1 {
            return endShape.pathData(rect)
        }

        let startPath = startShape.pathData(rect)
        let endPath = endShape.pathData(rect)

        return interpolatePaths(from: startPath, to: endPath, progress: progress, in: rect)
    }

    private func interpolatePaths(from start: Path, to end: Path, progress: Double, in rect: CGRect) -> Path {
        // Simple interpolation by scaling and fading
        // For more complex morphing, use CoreGraphics path interpolation
        var path = Path()

        if progress < 0.5 {
            // Shrink start shape
            let scale = 1.0 - (progress * 2) * 0.3
            path.addPath(start)
            let transform = CGAffineTransform(translationX: rect.midX, y: rect.midY)
                .scaledBy(x: scale, y: scale)
                .translatedBy(x: -rect.midX, y: -rect.midY)
            path = path.applying(transform)
        } else {
            // Grow end shape
            let scale = ((progress - 0.5) * 2) * 0.7 + 0.3
            path.addPath(end)
            let transform = CGAffineTransform(translationX: rect.midX, y: rect.midY)
                .scaledBy(x: scale, y: scale)
                .translatedBy(x: -rect.midX, y: -rect.midY)
            path = path.applying(transform)
        }

        return path
    }
}

// MARK: - Glass Morphing View

struct GlassMorphingView: View {
    let currentShape: GlassShape
    let targetShape: GlassShape
    let color: Color
    let size: CGFloat
    @State private var morphProgress: Double = 0
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        MorphingGlassShape(
            progress: morphProgress,
            startShape: currentShape,
            endShape: targetShape
        )
        .fill(
            LinearGradient(
                colors: [
                    color.opacity(0.6),
                    color.opacity(0.4)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .overlay(
            MorphingGlassShape(
                progress: morphProgress,
                startShape: currentShape,
                endShape: targetShape
            )
            .fill(.ultraThinMaterial)
            .opacity(0.3)
        )
        .overlay(
            MorphingGlassShape(
                progress: morphProgress,
                startShape: currentShape,
                endShape: targetShape
            )
            .strokeBorder(
                Color.white.opacity(colorScheme == .dark ? 0.3 : 0.5),
                lineWidth: 1.5
            )
        )
        .frame(width: size, height: size)
        .shadow(color: color.opacity(0.3), radius: 8, y: 4)
        .onAppear {
            withAnimation(MotionConfig.glassMorph) {
                morphProgress = 1.0
            }
        }
        .onChange(of: targetShape) { _ in
            morphProgress = 0
            withAnimation(MotionConfig.glassMorph) {
                morphProgress = 1.0
            }
        }
    }
}

// MARK: - View Extension for Glass Morphing

extension View {
    func glassMorphing(
        targetShape: GlassShape,
        animationSpec: Animation = MotionConfig.glassMorph
    ) -> some View {
        self.modifier(GlassMorphingModifier(targetShape: targetShape, animation: animationSpec))
    }
}

private struct GlassMorphingModifier: ViewModifier {
    let targetShape: GlassShape
    let animation: Animation
    @State private var currentShape: GlassShape = .circle

    func body(content: Content) -> some View {
        content
            .clipShape(
                MorphingGlassShape(
                    progress: currentShape == targetShape ? 1.0 : 0.0,
                    startShape: currentShape,
                    endShape: targetShape
                )
            )
            .onAppear {
                withAnimation(animation) {
                    currentShape = targetShape
                }
            }
            .onChange(of: targetShape) { newShape in
                withAnimation(animation) {
                    currentShape = newShape
                }
            }
    }
}

// MARK: - Semantic Shape Mapping

extension GlassShape {
    static func forCategory(_ categoryName: String) -> GlassShape {
        switch categoryName.uppercased() {
        case "WATER", "TRAVEL":
            return .wave
        case "FOOD", "ENTERTAINMENT":
            return .petal
        case "WORK", "SERVICE":
            return .hexagon
        case "FITNESS", "OUTDOOR":
            return .triangle
        case "HOME":
            return .roundedSquare
        default:
            return .circle
        }
    }

    static func forTransportType(_ typeName: String) -> GlassShape {
        switch typeName.uppercased() {
        case "WALK":
            return .petal
        case "BIKE":
            return .hexagon
        case "CAR", "TRAIN":
            return .roundedSquare
        case "PLANE":
            return .triangle
        case "BOAT":
            return .wave
        default:
            return .circle
        }
    }

    static func forTimeOfDay(hour: Int) -> GlassShape {
        switch hour {
        case 6..<12:  // Morning
            return .petal
        case 12..<18: // Afternoon
            return .roundedSquare
        case 18..<22: // Evening
            return .hexagon
        default:      // Night
            return .circle
        }
    }
}

// MARK: - Continuous Morphing Animation

struct ContinuousMorphingView: View {
    let shapes: [GlassShape]
    let color: Color
    let size: CGFloat
    let duration: Double
    @State private var currentIndex: Int = 0
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        GlassMorphingView(
            currentShape: shapes[currentIndex],
            targetShape: shapes[(currentIndex + 1) % shapes.count],
            color: color,
            size: size
        )
        .onAppear {
            startMorphing()
        }
    }

    private func startMorphing() {
        Timer.scheduledTimer(withTimeInterval: duration, repeats: true) { _ in
            withAnimation(MotionConfig.glassMorph) {
                currentIndex = (currentIndex + 1) % shapes.count
            }
        }
    }
}
