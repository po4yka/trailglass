import SwiftUI

// MARK: - Motion Configuration for Liquid Glass Design
// iOS 26 Liquid Glass spring animations with expressive, bouncy motion

/// Standard spring configurations for consistent motion
enum MotionConfig {
    // MARK: - Spring Responses

    /// Ultra-fast response (0.2s) - For immediate feedback like button presses
    static let instantResponse: Double = 0.2

    /// Fast response (0.3s) - For quick interactions
    static let fastResponse: Double = 0.3

    /// Medium response (0.4s) - Default for most interactions
    static let mediumResponse: Double = 0.4

    /// Slow response (0.6s) - For deliberate, graceful animations
    static let slowResponse: Double = 0.6

    /// Expressive response (0.8s) - For dramatic, playful animations
    static let expressiveResponse: Double = 0.8

    // MARK: - Damping Fractions

    /// High bounce (0.5) - Very bouncy, playful
    static let highBounce: Double = 0.5

    /// Medium bounce (0.6) - Balanced, expressive
    static let mediumBounce: Double = 0.6

    /// Low bounce (0.7) - Subtle bounce
    static let lowBounce: Double = 0.7

    /// Minimal bounce (0.8) - Almost no bounce
    static let minimalBounce: Double = 0.8

    /// No bounce (1.0) - Critically damped, no overshoot
    static let noBounce: Double = 1.0

    // MARK: - Standard Spring Animations

    /// Quick, snappy interaction (0.3s, 0.6 damping)
    static var snappy: Animation {
        .spring(response: fastResponse, dampingFraction: mediumBounce)
    }

    /// Smooth, balanced interaction (0.4s, 0.7 damping)
    static var smooth: Animation {
        .spring(response: mediumResponse, dampingFraction: lowBounce)
    }

    /// Bouncy, playful interaction (0.4s, 0.5 damping)
    static var bouncy: Animation {
        .spring(response: mediumResponse, dampingFraction: highBounce)
    }

    /// Expressive, dramatic interaction (0.8s, 0.6 damping)
    static var expressive: Animation {
        .spring(response: expressiveResponse, dampingFraction: mediumBounce)
    }

    /// Gentle, graceful interaction (0.6s, 0.8 damping)
    static var gentle: Animation {
        .spring(response: slowResponse, dampingFraction: minimalBounce)
    }

    /// Instant feedback (0.2s, 0.8 damping)
    static var instant: Animation {
        .spring(response: instantResponse, dampingFraction: minimalBounce)
    }

    // MARK: - Specific Use Cases

    /// Button press animation
    static var buttonPress: Animation {
        .spring(response: fastResponse, dampingFraction: mediumBounce)
    }

    /// Card appearance animation
    static var cardAppear: Animation {
        .spring(response: mediumResponse, dampingFraction: lowBounce)
    }

    /// Menu expansion animation
    static var menuExpand: Animation {
        .spring(response: slowResponse, dampingFraction: highBounce)
    }

    /// FAB rotation animation
    static var fabRotation: Animation {
        .spring(response: mediumResponse, dampingFraction: mediumBounce)
    }

    /// Loading indicator animation
    static var loadingPulse: Animation {
        .spring(response: expressiveResponse, dampingFraction: highBounce)
            .repeatForever(autoreverses: true)
    }

    /// Shape morphing animation
    static var shapeMorph: Animation {
        .spring(response: expressiveResponse, dampingFraction: mediumBounce)
    }

    /// Shimmer sweep animation
    static var shimmerSweep: Animation {
        .linear(duration: 1.5)
            .repeatForever(autoreverses: false)
    }

    // MARK: - Scale Effects

    /// Standard press scale (0.97)
    static let pressScale: CGFloat = 0.97

    /// Subtle press scale (0.98)
    static let subtlePressScale: CGFloat = 0.98

    /// Prominent press scale (0.95)
    static let prominentPressScale: CGFloat = 0.95

    /// Pop-in scale (1.05)
    static let popInScale: CGFloat = 1.05

    // MARK: - Timing

    /// Fast duration for linear animations (0.2s)
    static let fastDuration: Double = 0.2

    /// Medium duration for linear animations (0.3s)
    static let mediumDuration: Double = 0.3

    /// Slow duration for linear animations (0.5s)
    static let slowDuration: Double = 0.5

    /// Loading animation duration (2.0s)
    static let loadingDuration: Double = 2.0
}

// MARK: - Custom Spring Helper

extension Animation {
    /// Create a custom spring with Liquid Glass motion
    static func liquidGlass(
        response: Double = MotionConfig.mediumResponse,
        dampingFraction: Double = MotionConfig.mediumBounce,
        blendDuration: Double = 0
    ) -> Animation {
        .spring(
            response: response,
            dampingFraction: dampingFraction,
            blendDuration: blendDuration
        )
    }
}

// MARK: - Interactive State Animation Helper

struct InteractiveSpring: ViewModifier {
    @Binding var isPressed: Bool
    let scale: CGFloat
    let animation: Animation

    func body(content: Content) -> some View {
        content
            .scaleEffect(isPressed ? scale : 1.0)
            .animation(animation, value: isPressed)
    }
}

extension View {
    /// Apply interactive spring animation on press
    func interactiveSpring(
        isPressed: Binding<Bool>,
        scale: CGFloat = MotionConfig.pressScale,
        animation: Animation = MotionConfig.buttonPress
    ) -> some View {
        modifier(InteractiveSpring(isPressed: isPressed, scale: scale, animation: animation))
    }
}

// MARK: - Rotation Animation Helper

struct RotationAnimation: ViewModifier {
    let angle: Angle
    let animation: Animation

    func body(content: Content) -> some View {
        content
            .rotationEffect(angle)
            .animation(animation, value: angle)
    }
}

extension View {
    /// Apply rotation with animation
    func rotationAnimation(
        angle: Angle,
        animation: Animation = MotionConfig.fabRotation
    ) -> some View {
        modifier(RotationAnimation(angle: angle, animation: animation))
    }
}

// MARK: - Opacity Animation Helper

struct OpacityAnimation: ViewModifier {
    let opacity: Double
    let animation: Animation

    func body(content: Content) -> some View {
        content
            .opacity(opacity)
            .animation(animation, value: opacity)
    }
}

extension View {
    /// Apply opacity with animation
    func opacityAnimation(
        opacity: Double,
        animation: Animation = MotionConfig.smooth
    ) -> some View {
        modifier(OpacityAnimation(opacity: opacity, animation: animation))
    }
}

// MARK: - Offset Animation Helper

struct OffsetAnimation: ViewModifier {
    let x: CGFloat
    let y: CGFloat
    let animation: Animation

    func body(content: Content) -> some View {
        content
            .offset(x: x, y: y)
            .animation(animation, value: x)
            .animation(animation, value: y)
    }
}

extension View {
    /// Apply offset with animation
    func offsetAnimation(
        x: CGFloat = 0,
        y: CGFloat = 0,
        animation: Animation = MotionConfig.smooth
    ) -> some View {
        modifier(OffsetAnimation(x: x, y: y, animation: animation))
    }
}
