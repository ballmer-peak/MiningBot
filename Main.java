package main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.web.node.impl.bank.WebBankArea;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by Jonathan Carroll on 5/3/2017.
 */

@ScriptManifest(author = "Cyanwazoo", description = "Power Miner", name = "Miner 1.0", version = 1.0, category = Category.MINING)
public class Main extends AbstractScript {
    private Timer timer = new Timer(0);
    public static final Color BACKGROUND = new Color(0, 192, 192, 128);
    public static final int IRON_ID = 440;
    public static final int IRON_ROCK_COLOR_ID = 2576;
    private int[] ids;
    private GameObject currentNode;
    private boolean running = true;
    public ArrayList<GameObject> ironObjects;
    public int rotation;
    public int walkBack;

    public static final Area MINING_AREA = new Area(
            new Tile(2715, 3332, 0),
            new Tile(2714, 3332, 0),
            new Tile(2713, 3332, 0),
            new Tile(2715, 3331, 0),
            new Tile(2714, 3331, 0),
            new Tile(2713, 3331, 0),
            new Tile(2715, 3330, 0),
            new Tile(2714, 3330, 0),
            new Tile(2713, 3330, 0)
    );

    public void onStart() {
        getSkillTracker().start(Skill.MINING);
        timer = new Timer(0);
        ironObjects = new ArrayList<GameObject>();
        ironObjects.add(getGameObjects().getTopObjectOnTile(new Tile(2713, 3332, 0)));
        ironObjects.add(getGameObjects().getTopObjectOnTile(new Tile(2715, 3331, 0)));
        ironObjects.add(getGameObjects().getTopObjectOnTile(new Tile(2714, 3330, 0)));
        rotation = 0;
        walkBack = 0;
        ids
    }

    @Override
    public int onLoop() {
        if (running) {
            if (ableToMine()) {
                mine();
                walkBack = 0;
            } else if (ableToBank()) {
                bank();
            } else if (getInventory().isFull()) {
                getWalking().walk(WebBankArea.ARDOUGNE_SOUTH.getArea().getCenter());
            } else if (!getInventory().isFull() && !MINING_AREA.contains(getLocalPlayer()) && walkBack == 1) {
                getWalking().walk(MINING_AREA.getCenter());
            }
        }
        return running ? getReactionTime() : -1;
    }

    private void mine() {
        currentNode = ironObjects.get(rotation);
        if (rotation == 2){
            rotation = 0;
        }
        else{rotation++;}
        currentNode.interact("Mine");
        sleepUntil(() -> getLocalPlayer().getAnimation() == -1, Calculations.random(3000, 4000));
//        if (!getLocalPlayer().isAnimating()) {
//            if (currentNode != null && currentNode.getModelColors() != null && currentNode.exists()) {
//                if (getLocalPlayer().getAnimation()!=624){
//                    currentNode.interact("Mine");
//                }
//                sleepUntil(() -> getLocalPlayer().getAnimation() != -1, Calculations.random(3000, 4000));
//            }
//        }
    }

    private boolean bank(){
        boolean results = false;
        if (ableToBank()){
            if(getBank().openClosest()){
                if(getInventory().contains(IRON_ID)){
                    getBank().depositAllItems();
                }
                if(readyToMine() && getBank().close()){
                    results = true;
                    walkBack = 1;
                }
            }
        }
        return results;
    }

    private final Filter<GameObject> rockFilter = new Filter<GameObject>(){
        public boolean match(GameObject go){
            if(go == null || !go.exists() || go.getName() == null || !go.getName().equals("Rocks") || go.getModelColors().length <1)
                return false;
            boolean hasID = false;
            for(int i = 0; i < getIDs().length; i++){
                if(go.getID() == getIDs()[i]){
                    hasID = true;
                    currentNode = go;
                    break;
                }
            }
            if(!hasID)
                return false;
            if(go.distance(getLocalPlayer()) > 1)
                return false;
            return true;
        }
    };
    public int[] getIDs(){
        return ids;
    }

    public Filter<GameObject> getRockFilter(){
        return this.rockFilter;
    }

    public GameObject getIron() {
        List<GameObject> acceptable = getGameObjects().all(rockFilter);
        return getClosest(acceptable);
    }
    private GameObject getClosest(List<GameObject> rocks){
        GameObject currRock = null;
        double dist = Double.MAX_VALUE;
        for(GameObject go : rocks){
            if(currRock == null){
                currRock = go;
                dist = go.distance(getLocalPlayer());
                continue;
            }
            double tempDist = go.distance(getLocalPlayer());
            if(tempDist < dist){
                currRock = go;
                dist = tempDist;
            }
        }
        return currRock;
    }

    private boolean ableToMine(){
        return readyToMine() && MINING_AREA.contains(getLocalPlayer().getTile());
    }

    private boolean readyToMine(){
        return !getInventory().isFull();
    }

    private boolean ableToBank(){
        return getInventory().isFull() && WebBankArea.ARDOUGNE_SOUTH.getArea().contains(getLocalPlayer());
    }

    private int getReactionTime(){
//        return (int) Calculations.gRandom(Calculations.random(400,600),Calculations.random(80,120));
        return (int) Calculations.random(Calculations.random(1300,1700));
    }

    public void onPaint(Graphics2D graphics){
        graphics.setColor(BACKGROUND);
        graphics.fillRect(1,1,199,145);
        graphics.setColor(Color.BLACK);
        graphics.setStroke(new BasicStroke(2));
        graphics.drawRect(1,1,200,145);
        graphics.drawString("Total Time: "+timer.formatTime(),10,25);
        graphics.drawString("Exp Gained: "+getSkillTracker().getGainedExperience(Skill.MINING),10,75);
        graphics.drawString("Levels Gained: "+getSkillTracker().getGainedLevels(Skill.MINING),10,100);
        graphics.drawString("Has TargetL" + (currentNode != null ? currentNode.exists() : "false"), 10,120);
    }
}
