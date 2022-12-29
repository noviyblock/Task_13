package ru.vsu.cs.chernykh;

import java.util.*;

public class Model {
    private static final int fieldWidth = 4;
    private Tile[][] gameTiles;

    int score = 0;
    int maxTile = 2;

    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Integer> previousScores = new Stack<>();

    private boolean isSaveNeeded = true;
    public Model(){
        resetGameTiles();
    }

    private List<Tile> getEmptyTiles(){
        List<Tile> result = new ArrayList<>();
        for(int i=0;i<fieldWidth;i++){
            for(int j=0;j<fieldWidth;j++){
                if(gameTiles[i][j].value == 0) result.add(gameTiles[i][j]);
            }
        }

        return result;
    }

    private void addTile(){
        List<Tile> emptyTiles = getEmptyTiles();
        if(!emptyTiles.isEmpty()){
            int index = (int) (Math.random()*emptyTiles.size());
            Tile tile = emptyTiles.get(index);
            tile.value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    public void resetGameTiles(){
        gameTiles = new Tile[fieldWidth][fieldWidth];
        for(int i=0;i<fieldWidth;i++){
            for(int j=0;j<fieldWidth;j++){
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    private boolean compressTiles(Tile[] tiles){
        boolean changed = false;
        for(int i=1;i< tiles.length;i++){
            if(!tiles[i].isEmpty()){
                int index = i;
                while (index>0){
                    if(tiles[index-1].isEmpty()){
                        Tile temp = tiles[index-1];
                        tiles[index-1] = tiles[index];
                        tiles[index] = temp;
                        changed = true;
                    }
                    --index;
                }
            }
        }
        return changed;
    }

    private boolean mergeTiles(Tile[] tiles){
        boolean changed = false;
        for(int i=1;i<tiles.length;i++){
            if(!tiles[i].isEmpty()){
                if(tiles[i-1].value == tiles[i].value){
                    tiles[i-1].value *= 2;
                    tiles[i].value = 0;
                    changed = true;

                    int newValue = tiles[i-1].value;
                    score+=newValue;
                    if(newValue>maxTile) maxTile = newValue;
                }
            }
        }
        compressTiles(tiles);
        return changed;
    }

    void left(){
        if(isSaveNeeded) saveState(gameTiles);

        boolean changed = false;
        for(int i=0;i< gameTiles.length;i++){
            if(compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i])) changed = true;
        }

        if(changed) addTile();
        isSaveNeeded = true;
    }

    private void rotateGameTilesBy90Degrees(){
        Tile[][] newTile = new Tile[fieldWidth][fieldWidth];
        for(int i=0;i<fieldWidth;i++){
            for(int j=0;j<fieldWidth;j++){
                newTile[j][3-i] = gameTiles[i][j];
            }
        }
        gameTiles = newTile;
    }

    void down(){
        saveState(gameTiles);
        rotateGameTilesBy90Degrees();;
        left();
        rotateGameTilesBy90Degrees();
        rotateGameTilesBy90Degrees();
        rotateGameTilesBy90Degrees();
    }

    void right(){
        saveState(gameTiles);
        rotateGameTilesBy90Degrees();
        rotateGameTilesBy90Degrees();
        left();
        rotateGameTilesBy90Degrees();
        rotateGameTilesBy90Degrees();
    }

    void up(){
        saveState(gameTiles);
        rotateGameTilesBy90Degrees();
        rotateGameTilesBy90Degrees();
        rotateGameTilesBy90Degrees();
        left();
        rotateGameTilesBy90Degrees();
    }

    public Tile[][] getGameTiles(){
        return gameTiles;
    }

    private boolean isFull(){
        return getEmptyTiles().isEmpty();
    }

    public boolean canMove(){
        if(!isFull()) return true;
        for(int i=0;i<fieldWidth;i++){
            for(int j=0;j<fieldWidth;j++){
                Tile tile = gameTiles[i][j];
                if(((i<fieldWidth-1)&&(tile.value==gameTiles[i+1][j].value)) || ((j<fieldWidth-1) && (tile.value == gameTiles[i][i+1].value))) return true;
            }
        }
        return false;
    }

    private void saveState(Tile[][] tiles){
        Tile[][] tiles1 = new Tile[fieldWidth][fieldWidth];
        for(int i=0;i<fieldWidth;i++){
            for(int j=0;j<fieldWidth;j++){
                Tile tile = new Tile();
                tile.value = tiles[i][j].value;
                tiles1[i][j] = tile;
            }
        }
        previousStates.push(tiles1);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback(){
        if(!previousScores.isEmpty() && !previousStates.isEmpty()){
            score = previousScores.pop();
            gameTiles = previousStates.pop();
        }
    }

    void randomMove(){
        int random = (int) (Math.random()*100)%4;

        if(random==0) left();
        if(random==1) up();
        if(random==2) right();
        if(random==3) down();
    }

    boolean hasBoardChanged(){
        for(int i=0;i<fieldWidth;i++){
            for(int j=0;j<fieldWidth;j++){
                if(gameTiles[i][j].value != previousStates.peek()[i][j].value){
                    return true;
                }
            }
        }
        return false;
    }

    MoveEfficiency getMoveEfficiency(Move move){
        move.move();

        int emptyTiles = -1;
        int newScore = 0;

        if(hasBoardChanged()){
            emptyTiles = getEmptyTiles().size();
            newScore = score;
        }
        MoveEfficiency moveEfficiency = new MoveEfficiency(emptyTiles, newScore, move);
        rollback();

        return moveEfficiency;
    }

    void autoMove(){
        PriorityQueue<MoveEfficiency> priorityQueue = new PriorityQueue<>(4, Collections.reverseOrder());

        priorityQueue.offer(getMoveEfficiency(this::left));
        priorityQueue.offer(getMoveEfficiency(this::up));
        priorityQueue.offer(getMoveEfficiency(this::right));
        priorityQueue.offer(getMoveEfficiency(this::down));

        priorityQueue.peek().getMove().move();
    }
}