package ru.vsu.cs.chernykh;

import javax.swing.*;
import java.awt.*;

public class View extends JPanel {
    private static final Color backgroundColor = new Color(0xbbada0);
    private static final String fontName = "Arial";
    private static final int tileSize = 96;
    private static final int tileMargin = 12;

    private Controller controller;

    boolean isGameWon = false;
    boolean isGameLost = false;

    public View(Controller controller){
        setFocusable(true);
        this.controller = controller;
        addKeyListener(controller);
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);
        g.setColor(backgroundColor);
        g.fillRect(0,0,this.getSize().width,this.getSize().height);
        for(int x=0;x<4;x++){
            for(int y=0;y<4;y++){
                drawTile(g, controller.getGameTiles()[y][x], x, y);
            }
        }
        g.drawString("Score: "+controller.getScore(),140,465);
        if(isGameWon){
            JOptionPane.showMessageDialog(this,"You`ve won!");
        }else if(isGameLost){
            JOptionPane.showMessageDialog(this,"You`ve lost :(");
        }
    }

    private void drawTile(Graphics g2, Tile tile, int x, int y){
        Graphics2D g = ((Graphics2D) g2);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int value = tile.value;
        int xOffset = offsetCoors(x);
        int yOffset = offsetCoors(y);
        g.setColor(tile.getTileColor());
        g.fillRoundRect(xOffset,yOffset,tileSize,tileSize,8,8);
        g.setColor(tile.getFontColor());
        final int size = value < 100 ? 36 : value < 1000 ? 32 : 24;
        final  Font font = new Font(fontName, Font.BOLD, size);
        g.setFont(font);

        String s = String.valueOf(value);
        final FontMetrics fm = getFontMetrics(font);

        final int w = fm.stringWidth(s);
        final int h = -(int) fm.getLineMetrics(s,g).getBaselineOffsets()[2];

        if(value!=0) g.drawString(s,xOffset + (tileSize-w)/2, yOffset+tileSize-(tileSize-h)/2-2);
    }
    private static int offsetCoors(int arg){
        return arg*(tileMargin+tileSize)+tileMargin;
    }
}
