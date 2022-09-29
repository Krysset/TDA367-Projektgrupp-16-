package com.g16.feyrune.model.overworld.map;

import com.g16.feyrune.Util.Pair;
import com.g16.feyrune.Util.Parser;
import com.g16.feyrune.Util.Random;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handle all operations related to the parsing of map files.
 */
public class MapParser {
    /**
     * This method parses a map filed (produced by Tiled).
     *
     * @param filePath The file path of the map you want to parse.
     * @return A {@link Map} object containing the parsed map.
     */
    public static Map parseMapFile(String filePath) {
        Document doc = Parser.readXMLDocument(filePath);

        Pair<Integer, Integer> mapSize = getMapSize(doc);

        Pair<Point, Iterable<Transporter>> mapStartPosAndTransporters = getMapStartPos(doc);

        List<Integer> collisionIds = parseCollisionIds(doc);

        int[][] collisionList = generateCollisionIdList(doc, collisionIds, mapSize, true);

        int[][][] collisionMap = createCollisionMap(collisionList, mapSize);

        return generateTileMap(collisionMap, mapStartPosAndTransporters.getFst(), mapStartPosAndTransporters.getSnd());
    }

    /**
     * This method gets properties of the map.
     *
     * @param doc The document containing the map file.
     * @return A {@link Pair} containing the start position and an iterable of transporters on the map.
     */
    private static Pair<Point, Iterable<Transporter>> getMapStartPos(Document doc) {
        Point startPos = new Point();
        List<Transporter> transporters = new ArrayList<>();
        boolean isXSet = false;
        boolean isYSet = false;

        Node mapNode = doc.getElementsByTagName("map").item(0);
        NodeList mapChildNodes = mapNode.getChildNodes();
        // To avoid ghost nodes
        for (int i = 0; i < mapChildNodes.getLength(); i++) {
            Node currentMapChild = mapChildNodes.item(i);
            if (currentMapChild.getNodeName().equals("properties")) {
                NodeList propertyNodes = currentMapChild.getChildNodes();
                // Iterate through every property until we set a new x and y value
                for(int j = 0; j < propertyNodes.getLength(); j++) {
                    Node propertyNode = propertyNodes.item(j);
                    // Need to check if it's a property node because of ghost nodes
                    if(propertyNode.getNodeName().equals("property")) {
                        NamedNodeMap nodeAttributes = propertyNode.getAttributes();
                        if (nodeAttributes.getNamedItem("name") != null) {
                            String nodeName = nodeAttributes.getNamedItem("name").getNodeValue();

                            if (nodeName.equals("startx")) {
                                startPos.x = Integer.parseInt(nodeAttributes.getNamedItem("value").getNodeValue());
                                isXSet = true;
                            } else if (nodeName.equals("starty")) {
                                startPos.y = Integer.parseInt(nodeAttributes.getNamedItem("value").getNodeValue());
                                isYSet = true;
                            } else if(nodeName.startsWith("transporter")) {
                                // Looks like this in the map file:
                                // <property name="transporter1" value="assets/maps/map1.tmx,1,1,2,2"/>
                                String[] split = nodeAttributes.getNamedItem("value").getNodeValue().split(",");

                                String mapAssetPath = split[0];
                                int fromX = Integer.parseInt(split[1]);
                                int fromY = Integer.parseInt(split[2]);
                                int toX = Integer.parseInt(split[3]);
                                int toY = Integer.parseInt(split[4]);

                                Transporter transporter = new Transporter(mapAssetPath, new Point(fromX, fromY), new Point(toX, toY));
                                transporters.add(transporter);
                            }
                        }
                    }
                }
            }
        }

        if (!isXSet & !isYSet) {
            throw new RuntimeException("Startposition for map not found");
        }
        return new Pair<>(startPos, (Iterable<Transporter>) transporters);
    }

    /**
     * This method generates a Tile map from a collision map.
     *
     * @param collisionMap The collision map.
     * @return A {@link Map} based on the collision map.
     */
    private static Map generateTileMap(int[][][] collisionMap, Point startPos, Iterable<Transporter> transporters) {
        Tile[][] tiles = new Tile[collisionMap[0].length][collisionMap.length];

        for (int i = 0; i < collisionMap.length; i++) {
            for (int j = 0; j < collisionMap[0].length; j++) {
                boolean collision = false;
                for (int k = 0; k < collisionMap[i][j].length; k++){
                    if(collisionMap[i][j][k] == 1){
                        collision = true;
                        break;
                    }
                }
//                boolean encounter = (Random.randomInt(100)>90) ? true : false;
                boolean encounter = false;
                tiles[j][i] = new Tile(collision, encounter);
            }
        }

        addTransportersToTiles(tiles, transporters);

        return new Map(tiles, startPos.x, startPos.y);
    }

    /**
     * This method adds transporters to the tiles. Modifies in place.
     *
     * @param tiles The tiles.
     * @param transporters The transporters.
     */
    private static void addTransportersToTiles(Tile[][] tiles, Iterable<Transporter> transporters) {
        for(Transporter transporter : transporters) {
            tiles[transporter.getFromX()][transporter.getFromY()].setTransporter(transporter);
        }
    }

    /**
     * This method creates a collision map from a collision list, and a map size.
     *
     * @param collisionList A list of whether a tile has collision or not.
     * @param mapSize The size of the map.
     * @return The XML document.
     */
    private static int[][][] createCollisionMap(int[][] collisionList, Pair<Integer, Integer> mapSize) {
        int[][][] collisionMap = new int[mapSize.snd][mapSize.fst][collisionList[0].length];

        int c = 0;
        for (int i = mapSize.snd - 1; i >= 0; i--) {
            for (int j = 0; j < mapSize.fst; j++) {
                collisionMap[i][j] = collisionList[c];
                c++;
            }
        }

        return collisionMap;
    }

    /**
     * This method generates a list of whether a tile has collision or not.
     *
     * @param doc The XML document containing the map data.
     * @param gIds The list of IDs of tiles that have collision.
     * @param mapSize The size of the map.
     * @return A list of whether a tile has collision or not.
     */
    private static int[][] generateCollisionIdList(Document doc, List<Integer> gIds, Pair<Integer, Integer> mapSize, boolean printBinary) {
        // Get all the layer nodes, as collisions could exist on multiple layers.
        NodeList layerNodes = doc.getElementsByTagName("layer");

        int[][] collisionList = new int[mapSize.fst * mapSize.snd][layerNodes.getLength()];
        for (int i = 0; i < layerNodes.getLength(); i++) {

            NodeList childNodes = layerNodes.item(i).getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node dataNode = childNodes.item(j);

                if (dataNode.getNodeName().equals("data")) {
                    // Parse the CSV data.
                    String[] tileIds = dataNode.getTextContent().split(",");
                    for (int k = 0; k < tileIds.length; k++) {
                        // Prevents crashing because of whitespace or newlines when parsing integer.
                        tileIds[k] = tileIds[k].replaceAll("\\s+", "");
                        int tileId = Integer.parseInt(tileIds[k]);
                        if (gIds.contains(tileId)) {
                            collisionList[k][i] = printBinary ? 1 : tileId;
                        }
                    }
                }
            }
        }

        return collisionList;
    }



    /**
     * This method parses all the tile IDs that have collision.
     *
     * @param doc The XML document containing the map data.
     * @return The list of tile IDs that have collision.
     */
    private static ArrayList<Integer> parseCollisionIds(Document doc) {
        // All the for loops are required because of "ghost" child nodes.
        // Should be investigated in case of slow performance or similar.
        // Should also be refactored for better readability.

        NodeList tileNodes = doc.getElementsByTagName("tile");

        ArrayList<Integer> collisionIds = new ArrayList<>();
        for (int i = 0; i < tileNodes.getLength(); i++) {
            Node tileNode = tileNodes.item(i);

            // Get the id of the tile node.
            int id = Integer.parseInt(tileNode.getAttributes().getNamedItem("id").getNodeValue());

            NodeList tileNodeProperties = tileNode.getChildNodes();
            for (int j = 0; j < tileNodeProperties.getLength(); j++) {
                Node properties = tileNodeProperties.item(j);

                NodeList propertiesChildNodes = properties.getChildNodes();
                for (int k = 0; k < propertiesChildNodes.getLength(); k++) {
                    Node property = propertiesChildNodes.item(k);

                    if (property.getAttributes() != null) {
                        if (property.getAttributes().getNamedItem("name").getNodeValue().equals("Collision")) {
                            // Must be +1 because the Tiled adds 1 to the id when mapping the tiles.
                            collisionIds.add(id + 1);
                        }
                    }
                }
            }
        }

        return collisionIds;
    }

    /**
     * This method gets the size of the map.
     *
     * @param doc The XML document containing the map data.
     * @return The size of the map as a pair of (width, height).
     */
    private static Pair<Integer, Integer> getMapSize(Document doc) {
        Node mapNode = doc.getElementsByTagName("map").item(0);
        int width = Integer.parseInt(mapNode.getAttributes().getNamedItem("width").getNodeValue());
        int height = Integer.parseInt(mapNode.getAttributes().getNamedItem("height").getNodeValue());
        return new Pair<>(width, height);
    }
}