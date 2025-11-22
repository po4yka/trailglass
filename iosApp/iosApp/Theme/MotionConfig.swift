import SwiftUI

// MARK: - Motion Configuration
// Spring animation configurations for iOS 26 Liquid Glass design
// All animations use spring physics for natural, fluid motion

struct MotionConfig {
    // MARK: - Standard Spring Configurations

    static let standardSpring = Animation.spring(
        response: 0.4,
        dampingFraction: 0.75,
        blendDuration: 0
    )

    static let expressiveSpring = Animation.spring(
        response: 0.5,
        dampingFraction: 0.65,
        blendDuration: 0
    )

    static let quickSpring = Animation.spring(
        response: 0.25,
        dampingFraction: 0.8,
        blendDuration: 0
    )

    static let slowSpring = Animation.spring(
        response: 0.7,
        dampingFraction: 0.7,
        blendDuration: 0
    )

    static let bouncySpring = Animation.spring(
        response: 0.45,
        dampingFraction: 0.55,
        blendDuration: 0
    )

    static let smooth = Animation.spring(
        response: 0.4,
        dampingFraction: 0.75,
        blendDuration: 0
    )

    static let bouncy = Animation.spring(
        response: 0.5,
        dampingFraction: 0.6,
        blendDuration: 0
    )

    static let instant = Animation.spring(
        response: 0.15,
        dampingFraction: 0.9,
        blendDuration: 0
    )

    // MARK: - Interactive Constants

    static let pressScale: CGFloat = 0.97

    // MARK: - Specialized Animations

    static func offsetSpring(delay: Double = 0) -> Animation {
        Animation.spring(
            response: 0.5,
            dampingFraction: 0.7,
            blendDuration: 0
        )
        .delay(delay)
    }

    static func scaleSpring(delay: Double = 0) -> Animation {
        Animation.spring(
            response: 0.35,
            dampingFraction: 0.65,
            blendDuration: 0
        )
        .delay(delay)
    }

    static func alphaSpring(delay: Double = 0) -> Animation {
        Animation.spring(
            response: 0.3,
            dampingFraction: 0.75,
            blendDuration: 0
        )
        .delay(delay)
    }

    // MARK: - Glass-Specific Animations

    static let glassAppear = Animation.spring(
        response: 0.6,
        dampingFraction: 0.7,
        blendDuration: 0
    )

    static let glassDisappear = Animation.spring(
        response: 0.35,
        dampingFraction: 0.8,
        blendDuration: 0
    )

    static let glassInteractive = Animation.spring(
        response: 0.25,
        dampingFraction: 0.6,
        blendDuration: 0
    )

    static let glassMorph = Animation.spring(
        response: 0.55,
        dampingFraction: 0.68,
        blendDuration: 0
    )

    static let shapeMorph = Animation.spring(
        response: 0.6,
        dampingFraction: 0.7,
        blendDuration: 0
    )

    // MARK: - UI Component Animations

    static let buttonPress = Animation.spring(
        response: 0.2,
        dampingFraction: 0.7,
        blendDuration: 0
    )

    static let buttonRelease = Animation.spring(
        response: 0.3,
        dampingFraction: 0.6,
        blendDuration: 0
    )

    static let cardExpand = Animation.spring(
        response: 0.5,
        dampingFraction: 0.72,
        blendDuration: 0
    )

    static let cardCollapse = Animation.spring(
        response: 0.4,
        dampingFraction: 0.78,
        blendDuration: 0
    )

    static let sheetPresent = Animation.spring(
        response: 0.55,
        dampingFraction: 0.75,
        blendDuration: 0
    )

    static let sheetDismiss = Animation.spring(
        response: 0.4,
        dampingFraction: 0.82,
        blendDuration: 0
    )

    // MARK: - Navigation Animations

    static let navigationPush = Animation.spring(
        response: 0.5,
        dampingFraction: 0.78,
        blendDuration: 0
    )

    static let navigationPop = Animation.spring(
        response: 0.4,
        dampingFraction: 0.8,
        blendDuration: 0
    )

    // MARK: - List/Scroll Animations

    static let listItemAppear = Animation.spring(
        response: 0.4,
        dampingFraction: 0.75,
        blendDuration: 0
    )

    static let listItemDisappear = Animation.spring(
        response: 0.3,
        dampingFraction: 0.8,
        blendDuration: 0
    )

    static func staggeredAppear(index: Int, baseDelay: Double = 0.05) -> Animation {
        Animation.spring(
            response: 0.4,
            dampingFraction: 0.75,
            blendDuration: 0
        )
        .delay(Double(index) * baseDelay)
    }

    // MARK: - Loading/Progress Animations

    static let loadingPulse = Animation.spring(
        response: 0.6,
        dampingFraction: 0.5,
        blendDuration: 0
    )
    .repeatForever(autoreverses: true)

    static let shimmer = Animation.linear(duration: 1.5)
        .repeatForever(autoreverses: false)

    // MARK: - Gesture Animations

    static let dragStart = Animation.spring(
        response: 0.2,
        dampingFraction: 0.85,
        blendDuration: 0
    )

    static let dragEnd = Animation.spring(
        response: 0.4,
        dampingFraction: 0.7,
        blendDuration: 0
    )

    static let swipeGesture = Animation.spring(
        response: 0.35,
        dampingFraction: 0.75,
        blendDuration: 0
    )

    // MARK: - Custom Spring Builder

    static func customSpring(
        response: Double = 0.4,
        damping: Double = 0.75,
        delay: Double = 0
    ) -> Animation {
        Animation.spring(
            response: response,
            dampingFraction: damping,
            blendDuration: 0
        )
        .delay(delay)
    }
}

// MARK: - Animation Presets

extension Animation {
    static var glassStandard: Animation {
        MotionConfig.standardSpring
    }

    static var glassExpressive: Animation {
        MotionConfig.expressiveSpring
    }

    static var glassQuick: Animation {
        MotionConfig.quickSpring
    }

    static var glassSlow: Animation {
        MotionConfig.slowSpring
    }

    static var glassBouncy: Animation {
        MotionConfig.bouncySpring
    }
}

// MARK: - Timing Curves

struct TimingCurves {
    static let easeInOut = Animation.timingCurve(0.42, 0, 0.58, 1, duration: 0.4)
    static let easeIn = Animation.timingCurve(0.42, 0, 1, 1, duration: 0.4)
    static let easeOut = Animation.timingCurve(0, 0, 0.58, 1, duration: 0.4)
    static let sharp = Animation.timingCurve(0.4, 0, 0.6, 1, duration: 0.3)
    static let gentle = Animation.timingCurve(0.25, 0.1, 0.25, 1, duration: 0.5)
}

// MARK: - Duration Constants

struct AnimationDuration {
    static let instant: Double = 0.15
    static let quick: Double = 0.25
    static let normal: Double = 0.4
    static let slow: Double = 0.6
    static let verySlow: Double = 0.9

    // Specific use cases
    static let tooltip: Double = 0.2
    static let button: Double = 0.25
    static let card: Double = 0.4
    static let sheet: Double = 0.5
    static let page: Double = 0.6
}

// MARK: - View Extension for Animated Transitions

extension View {
    func animatedScale(
        isActive: Bool,
        activeScale: CGFloat = 0.97,
        animation: Animation = MotionConfig.scaleSpring()
    ) -> some View {
        self.scaleEffect(isActive ? activeScale : 1.0)
            .animation(animation, value: isActive)
    }

    func animatedOpacity(
        isVisible: Bool,
        animation: Animation = MotionConfig.alphaSpring()
    ) -> some View {
        self.opacity(isVisible ? 1 : 0)
            .animation(animation, value: isVisible)
    }

    func animatedOffset(
        isActive: Bool,
        offset: CGSize,
        animation: Animation = MotionConfig.offsetSpring()
    ) -> some View {
        self.offset(isActive ? offset : .zero)
            .animation(animation, value: isActive)
    }

    func springTransition(
        isPresented: Bool,
        animation: Animation = MotionConfig.standardSpring
    ) -> some View {
        self
            .scaleEffect(isPresented ? 1 : 0.9)
            .opacity(isPresented ? 1 : 0)
            .animation(animation, value: isPresented)
    }
}
