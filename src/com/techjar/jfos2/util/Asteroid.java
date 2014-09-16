package com.techjar.jfos2.util;

import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.client.RenderHelper;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.Color;
import org.newdawn.slick.geom.Circle;

import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.geom.ShapeRenderer;
import org.newdawn.slick.geom.Triangulator;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author Techjar
 */
public class Asteroid {
    private static final Random random = new Random();
    private static final int textureWidth = 64;
    private static final int textureHeight = 64;
    private Shape[] shapes;
    private Shape[] minimalShapes;
    private Shape collisionBox;
    private int vertexVBO;
    private int colorVBO;
    private int indices;
    private VBOData data;
    public float colorMult = -1;

    public Asteroid(Polygon body, Circle[] craters) {
        shapes = new Shape[craters.length * 2 + 2];
        minimalShapes = new Shape[craters.length + 1];
        body.setCenterX(0);
        body.setCenterY(0);
        Shape scaled = body.transform(Transform.createScaleTransform((body.getWidth() + 6) / body.getWidth(), (body.getHeight() + 6) / body.getHeight()));
        scaled.setCenterX(0);
        scaled.setCenterY(0);
        shapes[0] = scaled;
        shapes[1] = body;
        for (int i = 0; i < craters.length; i++) {
            Circle scaledCrater = new Circle(craters[i].getCenterX(), craters[i].getCenterY(), craters[i].getRadius() - 2);
            shapes[i * 2 + 2] = craters[i];
            shapes[i * 2 + 3] = scaledCrater;
        }
        minimalShapes[0] = body;
        System.arraycopy(craters, 0, minimalShapes, 1, craters.length);
    }

    public Shape getCollisionBox() {
        if (collisionBox == null) {
            collisionBox = new Polygon(shapes[0].getPoints());
            collisionBox.setCenterX(0);
            collisionBox.setCenterY(0);
        }
        return collisionBox;
    }

    public Shape[] getMinimalShapes() {
        return minimalShapes;
    }

    public void setupVBO() {
        if (vertexVBO != 0) throw new IllegalStateException("VBO already setup!");
        uploadVBOData(getVBOData());
    }

    public VBOData generateVBOData() {
        int vboIndices = 0;
        List<Float> floatList = new ArrayList<>();
        List<Byte> byteList = new ArrayList<>();
        if (colorMult < 0) colorMult = 0.5F + random.nextFloat() * 1.75F;
        for (int i = 0; i < shapes.length; i += 2) {
            Color color = new Color();
            float mult = colorMult;
            float saturation = 0.59F;
            float brightness = 0.15f;
            if (i >= 2) mult -= 0.5F;
            for (int l = 0; l < 2; l++) {
                if (l == 1) mult += 0.25F;
                if (i + l == 0) color.fromHSB(0, 0, 0.02F);
                else color.fromHSB(0.0861F, MathHelper.clamp(saturation / mult, 0, 1), MathHelper.clamp(brightness * mult, 0, 1));
                Shape sh = shapes[i + l];
                Triangulator triangles = sh.getTriangles();
                for (int j = 0; j < triangles.getTriangleCount(); j++) {
                    for (int k = 0; k < 3; k++) {
                        float[] point = triangles.getTrianglePoint(j, k % 3);
                        floatList.add(point[0]);
                        floatList.add(point[1]);
                        floatList.add((point[0] - sh.getMinX()) / textureWidth);
                        floatList.add((point[1] - sh.getMinY()) / textureHeight);
                        byteList.add(color.getRedByte());
                        byteList.add(color.getGreenByte());
                        byteList.add(color.getBlueByte());
                        vboIndices++;
                    }
                }
            }
        }
        float[] floatArray = new float[floatList.size()];
        for (int i = 0; i < floatArray.length; i++) {
            floatArray[i] = floatList.get(i);
        }
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = byteList.get(i);
        }
        return new VBOData(vboIndices, floatArray, byteArray);
    }

    public void uploadVBOData(VBOData data) {
        if (vertexVBO != 0) throw new IllegalStateException("VBO data already inserted!");
        indices = data.getIndices();
        float[] vertices = data.getVertices();
        byte[] colors = data.getColors();
        FloatBuffer floatBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        floatBuffer.put(vertices);
        floatBuffer.rewind();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(colors.length).order(ByteOrder.nativeOrder());
        byteBuffer.put(colors);
        byteBuffer.rewind();
        vertexVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexVBO);
        glBufferData(GL_ARRAY_BUFFER, floatBuffer, GL_STATIC_DRAW);
        colorVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, colorVBO);
        glBufferData(GL_ARRAY_BUFFER, byteBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public VBOData getVBOData() {
        if (data != null) return data;
        return data = generateVBOData();
    }

    public void render() {
        if (vertexVBO == 0) setupVBO();
        Client.getInstance().getTextureManager().getTexture("asteroid.png", GL_NEAREST).bind();
        // Setup vertex array
        glEnableClientState(GL_VERTEX_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, vertexVBO);
        glVertexPointer(2, GL_FLOAT, 16, 0);
        // Setup tex coord array
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, vertexVBO);
        glTexCoordPointer(2, GL_FLOAT, 16, 8);
        // Setup color array
        glEnableClientState(GL_COLOR_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, colorVBO);
        glColorPointer(3, GL_UNSIGNED_BYTE, 0, 0);
        // Draw
        glDrawArrays(GL_TRIANGLES, 0, indices);
        // Reset state
        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_COLOR_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void cleanup() {
        if (data == null) return;
        if (vertexVBO != 0) {
            glDeleteBuffers(vertexVBO);
            glDeleteBuffers(colorVBO);
            vertexVBO = 0;
            colorVBO = 0;
        }
        data = null;
    }

    public static class VBOData {
        private int indices;
        private float[] vertices;
        private byte[] colors;

        public VBOData(int indices, float[] vertices, byte[] colors) {
            this.indices = indices;
            this.vertices = vertices;
            this.colors = colors;
        }

        public int getIndices() {
            return indices;
        }

        public float[] getVertices() {
            return vertices;
        }

        public byte[] getColors() {
            return colors;
        }
    }
}
