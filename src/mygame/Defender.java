package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import java.util.ArrayList;
import java.util.Random;

public class Defender extends SimpleApplication {

    public static void main(String[] args) {
        Defender app = new Defender();
        //app.setShowSettings(false);
        app.start();

    }
    private Node shootables;
    private Node shootables2;
    private Node shootables3;
    private Node shootables4;
    private Node shootables5;
    private Geometry mark;
    boolean dragon = true;
    float z = -100f, x = -0.5f, y = -0.5f;
    Geometry cube;
    private Node naves;
    private boolean isRunning = true;
    int cont = 1, vida = 100, pontos = 0, contNaves = 0, contN = 0, contMortes = 0, fase = 1, numNaves = 15, cont2 = 0, recorde = 0;
    public String hit;
    public Spatial nave;

    @Override
    public void simpleInitApp() {
        initCrossHairs(); // um "+" no meio da tela para ajudar a mirar
        initKeys();       // carregar mapeamentos de teclas personalizados
        initMark();       // uma esfera vermelha para marcar o hit

        Texture west = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_west.jpg");
        Texture east = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_east.jpg");
        Texture north = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_north.jpg");
        Texture south = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_south.jpg");
        Texture up = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_up.jpg");
        Texture down = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_down.jpg");

        Spatial sky = SkyFactory.createSky(assetManager, west, east, north, south, up, down);
        rootNode.attachChild(sky);
        shootables = new Node("Shootables");
        shootables2 = new Node("Shootables2");
        shootables3 = new Node("Shootables3");
        shootables4 = new Node("Shootables4");
        shootables5 = new Node("Shootables4");
        naves = new Node("Ninjas");

        // rootNode.attachChild(shootables5);
        shootables2.detachChild(makeFloor());
        shootables3.detachChild(makeFloor());
        shootables4.detachChild(makeFloor());

        rootNode.attachChild(naves);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

    }

    /**
     * Declarando a ação "Disparar" e mapeando seus gatilhos.
     */
    private void initKeys() {
        inputManager.addMapping("Shoot",
                new KeyTrigger(KeyInput.KEY_SPACE), // trigger 1: spacebar
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT)); // trigger 2: left-button click
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("CriaNinja", new KeyTrigger(KeyInput.KEY_G));
        inputManager.addListener(actionListener, new String[]{"Pause", "CriaNinja", "Shoot"});
    }
    /**
     * Definindo a ação "Atirar": Determine o que foi atingido e como responder.
     */
    private ActionListener actionListener = new ActionListener() {

        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("Pause") && !keyPressed) {
                isRunning = !isRunning;
            }

            if (isRunning) {
                if (name.equals("CriaNinja") && keyPressed) {

                    Spatial nave = createNave(Integer.toString(cont));
                    naves.attachChild(nave);
                    cont++;
                }
                if (name.equals("RemoveNinjas") && keyPressed) {
                    for (Spatial s : naves.getChildren()) {
                        rootNode.detachChild(s);
                    }
                    naves.detachAllChildren();
                }
                if (name.equals("RemoveOlder") && keyPressed) {
                    int menor;
                    ArrayList<Integer> listaNum = new ArrayList<Integer>();

                    for (Spatial s : naves.getChildren()) {
                        listaNum.add(Integer.parseInt(s.getName()));
                    }

                    menor = listaNum.get(0);
                    for (int n : listaNum) {
                        if (n < menor) {
                            menor = n;
                        }
                    }

                    naves.detachChildNamed(Integer.toString(menor));
                    rootNode.detachChildNamed(Integer.toString(menor));
                }
                if (name.equals("DoubleSizeNewer") && keyPressed) {
                    int maior = 0;
                    ArrayList<Integer> listaNum = new ArrayList<Integer>();

                    for (Spatial s : naves.getChildren()) {
                        listaNum.add(Integer.parseInt(s.getName()));
                    }

                    for (int n : listaNum) {
                        if (n > maior) {
                            maior = n;
                        }
                    }
                    rootNode.getChild(Integer.toString(maior)).scale(2);
                }
            } else {
                System.out.println("Aperte P para voltar à aplicação");
            }
            if (name.equals("Shoot") && !keyPressed) {
                // 1. Redefinir lista de resultados.
                CollisionResults results = new CollisionResults();
                // 2. Aponte o raio da localização do came para a direção da came.
                Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                // 3. Aponte o raio do bloqueio do came para a direção da câmera.

                naves.collideWith(ray, results);
                // 4. Imprimir os resultados
                System.out.println("----- Collisions? " + results.size() + "-----");
                for (int i = 0; i < results.size(); i++) {
                    // Para cada acerto, sabemos a distância, o ponto de impacto, o nome da geometria.
                    float dist = results.getCollision(i).getDistance();
                    Vector3f pt = results.getCollision(i).getContactPoint();
                    hit = results.getCollision(i).getGeometry().getName();
                    System.out.println("---------------------------------------------------------" + i);
                    System.out.println("  ACERTOU " + hit + " at " + pt + ", " + dist + " wu away.");

                }
                // 5. Use os resultados (marcamos o objeto hit)
                if (results.size() > 0) {
                    //O ponto de colisão mais próximo é o que realmente foi atingido:
                    CollisionResult closest = results.getClosestCollision();
                    // Vamos interagir - marcamos o hit com um ponto vermelho.
                    mark.setLocalTranslation(closest.getContactPoint());
                    rootNode.attachChild(mark);

                    //
                } else {
                    // Sem hits? Em seguida, remova a marca vermelha.
                    rootNode.detachChild(mark);
                }
            }
        }
    };

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
        if (!isRunning) {
            if (cont2 == 0) {
                guiNode.detachAllChildren();
                guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
                BitmapText helloText = new BitmapText(guiFont, false);
                helloText.setSize(guiFont.getCharSet().getRenderedSize());
                helloText.setText("PAUSE");
                helloText.setLocalTranslation(550, 500, 0);
                guiNode.attachChild(helloText);
            } else if (cont2 == 1) {
                BitmapText TextGameOver = new BitmapText(guiFont, false);
                TextGameOver.setSize(guiFont.getCharSet().getRenderedSize());
                TextGameOver.setText("GAME OVER ");
                TextGameOver.setLocalTranslation(550, 500, 0);
                guiNode.attachChild(TextGameOver);
                BitmapText TextRecorde = new BitmapText(guiFont, false);
                TextRecorde.setSize(guiFont.getCharSet().getRenderedSize());
                TextRecorde.setText("Recorde: " + recorde);
                TextRecorde.setLocalTranslation(550, 400, 0);
                guiNode.attachChild(TextRecorde);
            }
        } else {
            guiNode.detachAllChildren();
            initCrossHairs();
        }

        if (isRunning) {
            BitmapText TextVida = new BitmapText(guiFont, false);
            TextVida.setSize(guiFont.getCharSet().getRenderedSize());
            TextVida.setText("Vida: " + vida);
            TextVida.setLocalTranslation(500, 800, 0);
            guiNode.attachChild(TextVida);
            BitmapText TextPontos = new BitmapText(guiFont, false);
            TextPontos.setSize(guiFont.getCharSet().getRenderedSize());
            TextPontos.setText("Pontos: " + pontos);
            TextPontos.setLocalTranslation(600, 800, 0);
            guiNode.attachChild(TextPontos);
            BitmapText TextFase = new BitmapText(guiFont, false);
            TextFase.setSize(guiFont.getCharSet().getRenderedSize());
            TextFase.setText("Fase: " + fase);
            TextFase.setLocalTranslation(550, 850, 0);
            guiNode.attachChild(TextFase);
            contN++;
            Random rnd = new Random();
            float x2 = (float) rnd.nextDouble();
            int y2 = rnd.nextInt(12);
            int z2 = rnd.nextInt(150);
            if (z2 >= 50) {
                z2 = z2 - 30;
            }
            z2 = z2 * -1;
            x2 = x2 * -1;

            if (contN < 1000) {

                if (contNaves < numNaves) {
                    contNaves++;
                    nave = createNave(Integer.toString(cont));
                    if (x * (25 * contNaves * -1) < 49.83607) {
                        nave.move(-50 + x * (25 * contNaves * -1), y2 + 3, z2 - 90);
                    } else if ((100 - x * (25 * contNaves * -1)) <= -36.90996) {
                        nave.move(-155 - x * (25 * contNaves * 1), y2 - 10, z2 - 80);
                    } else if ((x * (25 * contNaves * -1)) > 55.68604) {
                        nave.move(-55 - x * (25 * contNaves * 1), y2 + 10, z2 - 70);
                    } else {
                        nave.move(100 - x * (25 * contNaves * -1), y2, z2 - 50);
                    }
                    naves.attachChild(nave);
                    cont++;
                }
            } else {
                contN = 0;
            }
            for (Spatial s : naves.getChildren()) {
                s.move(0, 0, tpf * 15);
                if (s.getName() == hit) {
                    naves.detachChild(s);
                    rootNode.detachChild(s);
                    contMortes++;
                    if (contMortes == contNaves && contNaves == numNaves) {
                        contNaves = 0;
                        contMortes = 0;
                        fase += 1;
                        numNaves += 3;
                    }
                    pontos += 100;
                    if(pontos > recorde){
                        recorde = pontos;
                    }
                } else if (s.getLocalTranslation().z > cam.getLocation().z) {
                    naves.detachChild(s);
                    rootNode.detachChild(s);
                    contMortes++;
                    if (contMortes == contNaves && contNaves == numNaves) {
                        contNaves = 0;
                        contMortes = 0;
                        fase += 1;
                        numNaves += 3;
                    }
                    vida -= 10;
                    if (vida < 1) {
                        naves.detachAllChildren();
                        fase = 1;
                        contNaves = 0;
                        pontos = 0;
                        vida = 100;
                        numNaves = 15;
                        contMortes = 0;
                        cont2 = 1;
                        isRunning = !isRunning;
                    }
                }
            }

        }

    }

    public Spatial createNave(String name) {
        /**
         * Load a model. Uses model and texture from jme3-test-data library!
         */
        Spatial nave = assetManager.loadModel("Models/nave/nave.j3o");
        nave.setName(name);
        nave.scale(0.475f);
        nave.setLocalTranslation(x, y, z);
        nave.rotate(0, 0, 0);

        return nave;
    }

    /**
     * Um piso para mostrar que o "tiro" pode passar por vários objetos.
     */
    protected Geometry makeFloor() {
        Box box = new Box(40, .2f, 40);
        Geometry floor = new Geometry("the Floor", box);
        floor.setLocalTranslation(0, -4, -5);
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Blue);
        floor.setMaterial(mat1);
        return floor;
    }

    /**
     * Uma bola vermelha que marca o último ponto que foi "atingido" pelo
     * "tiro".
     */
    protected void initMark() {
        Sphere sphere = new Sphere(30, 30, 0.05f);
        mark = new Geometry("BOOM!", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.Red);
        mark.setMaterial(mark_mat);
    }

    /**
     * Um sinal de mais centrado para ajudar o jogador a mirar.
     */
    protected void initCrossHairs() {
        setDisplayStatView(false);
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
                settings.getWidth() / 2 - ch.getLineWidth() / 2, settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }

}
