package br.com.munif.munifotos.desktop.munifotosdesktop.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 *
 * @author munif
 */
public class ImagePanel extends JPanel {

    private BufferedImage image;

    public ImagePanel() {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        setMinimumSize(new Dimension(640, 480));
        setPreferredSize(new Dimension(640, 480));
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            double larguraTela=this.getWidth();
            double alturaTela=this.getHeight();
            double larguraImagem=image.getWidth();
            double alturaImagem=image.getHeight();
            double ratio=larguraImagem/alturaImagem;
            double larguraView=larguraTela;
            double alturaView=larguraTela/ratio;
            
            if (alturaView>alturaTela){
                alturaView=alturaTela;
                larguraView=alturaView*ratio;
            }
            g.drawImage(image, (int)(larguraTela/2-larguraView/2), (int)(alturaTela/2-alturaView/2),(int)larguraView,(int)alturaView,  this);
        }
    }

}
