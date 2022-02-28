package utils;

import config.AppfileConfig;
import config.SpringContext;
import entity.BBox;
import entity.Coordinate;
import entity.Polygon;
import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class HeatMap {
    private static final int HALCIRCLEPICSIZE = 32;
    private static final String CIRCLEPIC = "bolilla.png";
    private static final String SPECTRUMPIC = "colors.png";
    private HashMap<Integer, List<Polygon>> polygonMap;
    private int maxOccurance = 1;
    private int maxXValue;
    private int maxYValue;
    private final BufferedImage lvlMap;
    AppfileConfig appfileConfig= SpringContext.context.getBean("appfileConfig", AppfileConfig.class);
    public <T> HeatMap(BufferedImage lvlMap){
        this.lvlMap = lvlMap;

    }


    private <T> HashMap initMap(final List<T> points){
        HashMap map;
        if (appfileConfig.modelType == "scrfd"){
            map = new HashMap<Integer, List<BBox>>();
        }else{
            map = new HashMap<Integer, List<Polygon>>();
        }

        final BufferedImage mapPic = lvlMap;
        maxXValue = mapPic.getWidth();
        maxYValue = mapPic.getHeight();

        final int pointSize = points.size();
        points.forEach(point ->{
            final int hash = getkey(point);
            if (map.containsKey(hash)){
                List<T> thisList = (List<T>) map.get(hash);
                thisList.add(point);
                if (thisList.size()>maxOccurance){
                    maxOccurance = thisList.size();
                }
            }else{
                final List<T> newList = new LinkedList<T>();
                newList.add(point);
                map.put(hash, newList);
            }
        });
        return map;
    }

    public <T> BufferedImage createHeatMap(final float multiplier, List<T> points){
        HashMap<Integer, List<T>> map = initMap(points);


        BufferedImage heatMap = new BufferedImage(maxXValue, maxYValue, 6);
//        paintInColor(heatMap, Color.white);
        paintInColor(heatMap,Color.gray);

        final Iterator<List<T>> iterator = map.values().iterator();
        while (iterator.hasNext()){
            final List<T> currentPoints = iterator.next();

            float opaque = currentPoints.size()/(float) maxOccurance;

            opaque *= multiplier;
            if (opaque>1){
                opaque = 1;
            }

            T currentPoint = currentPoints.get(0);
            if (currentPoint instanceof BBox){
                BBox box = (BBox) currentPoint;
                addSpotHeat(heatMap, opaque, box);
            }else{
                if (currentPoint instanceof Polygon){
                    Polygon polygon = (Polygon) currentPoint;
                    addPolygon(heatMap, opaque, polygon);
                }
            }
//            addImage(heatMap, circle, opaque, currentPoint);


        }
//        try{
//            ImageIO.write(heatMap,"png",new File("raw.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        heatMap = negateImage(heatMap);
//        try{
//            ImageIO.write(heatMap,"png",new File("negative.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        remap(heatMap);

        final BufferedImage output = lvlMap;
        addImage(output, heatMap, 0.4f);

        return output;
    }

    private void remap(final BufferedImage heatMapBW){
        final BufferedImage colorGradiant = loadImage(SPECTRUMPIC);
        final int width = heatMapBW.getWidth();
        final int height= heatMapBW.getHeight();
        final int gradientHight = colorGradiant.getHeight()-1;

        for (int i=0;i<width;i++){
            for (int j=0; j<height;j++){

                final int rGB = heatMapBW.getRGB(i,j);

                float multiplier = rGB & 0xFF;
                multiplier*=((rGB >>> 8)) & 0xff;
                multiplier*=(rGB >>> 16) & 0xff;

                multiplier/=16581375;

                final int y = (int) (multiplier*gradientHight);
//                if (multiplier>0){
//                    System.out.println("b "+(rGB & 0xFF)+" g "+(((rGB >>> 8)) & 0xff) + " r "+(multiplier*=(rGB >>> 16) & 0xff));
//                    System.out.println("mul "+multiplier+" y "+y);
//                }
                final int mapedRGB = colorGradiant.getRGB(0,y);

                heatMapBW.setRGB(i,j, mapedRGB);

            }
        }
    }

    private BufferedImage negateImage(BufferedImage img){
        final int width = img.getWidth();
        final int height = img.getHeight();

        for (int i=0;i<width;i++){
            for (int j=0; j<height;j++){

                final int rGB = img.getRGB(i,j);

                final int b = Math.abs((rGB & 0xFF)-255);
                final int g =Math.abs(((rGB >>> 8) & 0xff)-255);
                final int r = Math.abs(((rGB >>> 16) &0xff)-255);

                img.setRGB(i,j,(r << 16) | (g <<8 ) | b);

            }
        }


        return img;
    }

    private void paintInColor(final BufferedImage buff, final Color color){
        final Graphics2D g2 = buff.createGraphics();
        g2.setColor(color);
        g2.fillRect(0,0,buff.getWidth(), buff.getHeight());
        g2.dispose();
    }

    private void addImage(final BufferedImage buff1, final BufferedImage buff2, final float opaque){
        addImage(buff1,buff2,opaque,null);
    }

    private void addSpotHeat(BufferedImage buff1, final float opaque, BBox box){

        Short color = (short) (255 * opaque);

        Color c1 = new Color(255, 255, 255, color);
        Color c2 = new Color(0, 0, 0, color);

        Graphics2D g2d = buff1.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER ,opaque));
        g2d.setColor(c2);
//        g2d.setPaint(gradient);

        g2d.fillOval(box.getX1(), box.getY1(), box.getW(), box.getH());
        g2d.dispose();

    }

    private void addPolygon(BufferedImage buff1, final float opaque, Polygon polygon){

        Short color = (short) (255 * opaque);

        Color c1 = new Color(255, 255, 255, color);
        Color c2 = new Color(0, 0, 0, color);


        Graphics2D g2d = buff1.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER ,opaque));
        g2d.setColor(c2);
//        g2d.setPaint(gradient);

        List<Coordinate> coords = polygon.getCoords();
        int[] x = new int[coords.size()];
        for (int i=0;i<coords.size();i++){
            x[i] = coords.get(i).getX();
        }

        int[] y = new int[coords.size()];
        for (int i=0;i<coords.size();i++){
            y[i] = coords.get(i).getY();
        }

        g2d.fillPolygon(x, y, 4);
        g2d.dispose();

    }

    private void addImage(BufferedImage buff1, BufferedImage buff2, final float opaque, BBox box){
        int x = 0;
        int y = 0;
        if (box!=null){
            int size = Math.max(box.getH(), box.getW());
            buff2 = createFadedCircleImage(box.getW(), box.getH());
            x = box.getX1()+ box.getW()/2;
            y = box.getY1()+ box.getH()/2;
//            System.out.println(x+" "+y);

        }
        Graphics2D g2d = buff1.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,opaque));

        g2d.drawImage(buff2,x,y,null);
        g2d.dispose();

    }

    public static BufferedImage createFadedCircleImage(int width, int height) {
        float radius = (width+height) / 2f;
        RadialGradientPaint gradient =
                new RadialGradientPaint(radius, radius, radius, new float[] {0f, 1f}, new Color[] {
                        Color.BLACK, new Color(0xffffffff, true)});
        BufferedImage im = createCompatibleTranslucentImage(width, height);
        Graphics2D g = (Graphics2D) im.getGraphics();
        g.setPaint(gradient);
        g.fillRect(0, 0, width, height);

        g.dispose();
        return im;
    }

    public static BufferedImage createCompatibleTranslucentImage(int width, int height) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();
        GraphicsConfiguration conf = dev.getDefaultConfiguration();
        return conf.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
    }

    private static BufferedImage resize(BufferedImage img, int newW, int newH){
        Image tmp =  img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp,0,0,null);
        g2d.dispose();
        return dimg;
    }

    private BufferedImage loadImage(String ref){
        BufferedImage b1 = null;
        try{
            b1 = ImageIO.read(new File(ref));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b1;
    }

    private <T> int getkey(final T p){
        if (p instanceof BBox){
            BBox box = (BBox) p;
            int x = box.getX1()+ box.getW()/2;
            int y = box.getY1()+ box.getH()/2;
            int coordHash = (x << 19) | ( y << 7);
            int sizeHash = (box.getW() <<19) | (box.getH() << 7);
            return (coordHash+sizeHash);
        }

        if (p instanceof Polygon){
            int coordHash = 0;
            Polygon polygon = (Polygon) p;
            for (Coordinate coord: polygon.getCoords()){
                coordHash+=(((int) coord.getX()<<19) | ((int) coord.getY() << 7));
            }
            return (coordHash);
        }
        return 0;

    }


}
