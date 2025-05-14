package com.duyphuc.olympics.animation;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleSystem {
private final Canvas canvas;
private final GraphicsContext gc;
private AnimationTimer animationTimer;
private final List<Particle> particles = new ArrayList<>();
private final Random random = new Random();

// Olympic colors
private final Color[] olympicColors = {
        Color.valueOf("#0085C7"), // Blue
        Color.valueOf("#F4C300"), // Yellow
        Color.valueOf("#000000"), // Black
        Color.valueOf("#009F3D"), // Green
        Color.valueOf("#DF0024")  // Red
};

private double mouseX = 0;
private double mouseY = 0;

public ParticleSystem(Canvas canvas) {
    this.canvas = canvas;
    this.gc = canvas.getGraphicsContext2D();

    // Check if Scene is already available
    if (canvas.getScene() != null) {
        setupMouseListener();
    } else {
        // If not, add a listener to set up mouse tracking when the Scene becomes available
        canvas.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                setupMouseListener();
            }
        });
    }
    
    // REMOVED THE PROBLEMATIC LINE:
    // canvas.getScene().setOnMouseMoved(e -> {
    //     mouseX = e.getSceneX();
    //     mouseY = e.getSceneY();
    // });
    
    // Create initial particles
    createInitialParticles();
}

private void createInitialParticles() {
    for (int i = 0; i < 100; i++) {
        particles.add(createRandomParticle());
    }
}

private void setupMouseListener() {
    // Ensure the scene is available before setting the mouse moved handler.
    // This method is called either when the scene is initially available
    // or when the sceneProperty listener detects a new scene.
    if (canvas.getScene() != null) {
        canvas.getScene().setOnMouseMoved(event -> {
            // Using event.getX() and event.getY() for coordinates relative to the canvas
            mouseX = event.getX();
            mouseY = event.getY();
        });
    }
}

private Particle createRandomParticle() {
    double x = random.nextDouble() * canvas.getWidth();
    double y = random.nextDouble() * canvas.getHeight();
    
    double speedX = (random.nextDouble() - 0.5) * 0.7;
    double speedY = (random.nextDouble() - 0.5) * 0.7;
    
    double size = 2 + random.nextDouble() * 3;
    double lifespan = 200 + random.nextDouble() * 200;
    
    Color color = olympicColors[random.nextInt(olympicColors.length)];
    color = color.deriveColor(0, 1, 1, 0.6); // Make it semi-transparent
    
    return new Particle(x, y, speedX, speedY, size, lifespan, color);
}

public void startAnimation() {
    animationTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            update();
            render();
        }
    };
    animationTimer.start();
}

public void stopAnimation() {
    if (animationTimer != null) {
        animationTimer.stop();
    }
}

private void update() {
    // Add new particles occasionally
    if (random.nextDouble() < 0.3) {
        particles.add(createRandomParticle());
    }
    
    // Update existing particles
    for (int i = particles.size() - 1; i >= 0; i--) {
        Particle p = particles.get(i);
        
        // Apply mouse influence
        double dx = mouseX - p.x;
        double dy = mouseY - p.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 150) {
            double force = (150 - distance) / 1500.0; // Ensure floating point division
            p.speedX += dx * force;
            p.speedY += dy * force;
        }
        
        // Update position
        p.x += p.speedX;
        p.y += p.speedY;
        
        // Apply friction
        p.speedX *= 0.98;
        p.speedY *= 0.98;
        
        // Update lifespan
        p.lifespan--;
        
        // Remove dead particles
        if (p.lifespan <= 0 || p.x < 0 || p.x > canvas.getWidth() || 
            p.y < 0 || p.y > canvas.getHeight()) {
            particles.remove(i);
        }
    }
}

private void render() {
    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    
    // Draw each particle
    for (Particle p : particles) {
        gc.setFill(p.color);
        gc.fillOval(p.x - p.size/2, p.y - p.size/2, p.size, p.size);
    }
}

private static class Particle {
    double x, y;
    double speedX, speedY;
    double size;
    double lifespan;
    Color color;
    
    Particle(double x, double y, double speedX, double speedY, 
             double size, double lifespan, Color color) {
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
        this.size = size;
        this.lifespan = lifespan;
        this.color = color;
    }
}
}