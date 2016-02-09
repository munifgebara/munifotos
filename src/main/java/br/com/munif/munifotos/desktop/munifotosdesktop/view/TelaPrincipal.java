/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.munif.munifotos.desktop.munifotosdesktop.view;

import br.com.munif.munifotos.desktop.munifotosdesktop.util.MetadataExample;
import com.sun.management.VMOption;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.text.DefaultCaret;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import static org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants.TIFF_TAG_DATE_TIME;

/**
 *
 * @author munif
 */
public class TelaPrincipal extends JFrame implements ActionListener {

    //                                                   2014:01:09 10:52:00
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
    private SimpleDateFormat sdfPasta = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat sdfFileName = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private JPanel painelSuperior;
    private JButton btPesquisa;
    private ImagePanel painelFoto;
    private JTextArea jtaMensagens;
    private List<File> images;
    private JButton btProximo;
    private JButton btAnterior;
    private int indice;
    private JButton btOrganiza;
    private JScrollPane jScrollPane;

    public TelaPrincipal() {
        super("MuniFotos");
        images = Collections.EMPTY_LIST;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        iniciaTela();
        pack();
        setVisible(true);
    }

    private void iniciaTela() {
        setLayout(new BorderLayout(3, 3));
        painelSuperior = new JPanel(new FlowLayout());
        painelSuperior.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        add(painelSuperior, BorderLayout.NORTH);
        btPesquisa = new JButton("Search");
        btPesquisa.addActionListener(this);
        btAnterior = new JButton("Previous");
        btAnterior.addActionListener(this);
        btProximo = new JButton("Next");
        btProximo.addActionListener(this);
        btOrganiza = new JButton("Organize");
        btOrganiza.addActionListener(this);

        painelSuperior.add(btPesquisa);
        painelSuperior.add(btAnterior);
        painelSuperior.add(btProximo);
        painelSuperior.add(btOrganiza);
        painelFoto = new ImagePanel();
        add(painelFoto, BorderLayout.CENTER);
        jtaMensagens = new JTextArea(10, 80);
        DefaultCaret caret = (DefaultCaret) jtaMensagens.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        jScrollPane = new JScrollPane(jtaMensagens);
        add(jScrollPane, BorderLayout.SOUTH);
        log("Hello!");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(btPesquisa)) {
            carregaImagens();
        } else if (e.getSource().equals(btProximo)) {
            indice++;
            if (indice >= images.size()) {
                indice = 0;
            }
            atualizaimagem();
        } else if (e.getSource().equals(btAnterior)) {
            indice--;
            if (indice < 0) {
                indice = images.size() - 1;
            }
            atualizaimagem();
        } else if (e.getSource().equals(btOrganiza)) {
            organiza();
        }

    }

    private void carregaImagens() throws HeadlessException {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            log("Folder selected:" + jFileChooser.getSelectedFile().toString());
            images = new ArrayList<>();
            indice = 0;
            processaPasta(jFileChooser.getSelectedFile());
            log(images.size() + " images selected");
            atualizaimagem();
        }
    }

    private void atualizaimagem() {
        try {
            if (images.size() > 0) {
                log("Carregando " + (indice + 1) + "/" + images.size() + " " + images.get(indice));
                painelFoto.setImage(ImageIO.read(images.get(indice)));
            }
        } catch (IOException ex) {
            log(ex.toString());
        }
    }

    private void log(String msg) {
        jtaMensagens.append(sdf.format(new Date()) + " " + msg + "\n");
        setTitle("Munifotos " + msg);
    }

    private void processaPasta(File folder) {
        try {
            log("Processing folder " + folder);
            if (folder.isDirectory()) {
                for (File f : folder.listFiles()) {
                    if (f.isDirectory()) {
                        processaPasta(f);
                    } else if (f.getName().toLowerCase().endsWith(".jpg")) {
                        images.add(f);
                    }
                }
            }
        } catch (Exception ex) {
            log(ex.toString());
        }
    }

    private void organiza() {
        File destino = null;
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            log("Destination folder selected:" + jFileChooser.getSelectedFile().toString());
            indice = 0;
            destino = jFileChooser.getSelectedFile();
            for (int i = 0; i < images.size(); i++) {
                indice = i;
                atualizaimagem();
                copiaImagem(images.get(i), destino);
                painelFoto.paint(painelFoto.getGraphics());
            }

        }

    }

    private void copiaImagem(File origem, File caminhoDestino) {
        try {
            ImageMetadata metadata = Imaging.getMetadata(origem);
            File pastaMapa = new File(caminhoDestino + "/maps/");
            pastaMapa.mkdirs();
            String pastaDestino = caminhoDestino.toString() + "/no date";
            if (metadata instanceof JpegImageMetadata) {
                Date dataFoto = new Date();
                JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                TiffField field = jpegMetadata.findEXIFValueWithExactMatch(TIFF_TAG_DATE_TIME);
                if (field != null) {
                    dataFoto = sdf.parse(field.getStringValue());
                    pastaDestino = caminhoDestino.toString() + "/" + sdfPasta.format(dataFoto) + "/";
                }

                final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
                try {
                    if (null != exifMetadata) {
                        final TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
                        if (null != gpsInfo) {
                            final String gpsDescription = gpsInfo.toString();
                            final double longitude = gpsInfo.getLongitudeAsDegreesEast();
                            final double latitude = gpsInfo.getLatitudeAsDegreesNorth();
                            System.out.println("    "
                                    + "GPS Longitude (Degrees East): " + longitude);
                            System.out.println("    "
                                    + "GPS Latitude (Degrees North): " + latitude);

                            BufferedImage original = ImageIO.read(origem);
                            BufferedImage destino = new BufferedImage(1920, (int) (original.getHeight() * (1920.0 / original.getWidth())), BufferedImage.TYPE_INT_RGB);
                            URL website = new URL("https://maps.googleapis.com/maps/api/staticmap?size=480x" + ((int) (original.getHeight() * (480.0 / original.getWidth()))) + "&maptype=roadmap&zoom=12&markers=color:red|" + latitude + "," + longitude);
                            BufferedImage mapa = ImageIO.read(website);
                            destino.getGraphics().drawImage(original, 0, 0, destino.getWidth(), destino.getHeight(), this);
                            destino.getGraphics().drawImage(mapa, destino.getWidth() - mapa.getWidth(), destino.getHeight() - mapa.getHeight(), this);

                            ImageIO.write(destino, "jpg", new File(caminhoDestino.toString() + "/maps/map-" + sdfFileName.format(dataFoto) + ".jpg"));

                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
            File pd = new File(pastaDestino);
            pd.mkdirs();
            File destino = new File(pd, origem.getName());
            Files.copy(origem.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception ex) {
            Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
