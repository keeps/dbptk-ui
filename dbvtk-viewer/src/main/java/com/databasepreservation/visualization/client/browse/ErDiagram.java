package com.databasepreservation.visualization.client.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerForeignKey;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerTable;
import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ErDiagram extends Composite {
  private static Map<String, ErDiagram> instances = new HashMap<>();

  public static ErDiagram getInstance(ViewerDatabase database, ViewerSchema schema) {
    String separator = "/";
    String code = database.getUUID() + separator + schema.getUUID();

    ErDiagram instance = instances.get(code);
    if (instance == null) {
      instance = new ErDiagram(database, schema);
      instances.put(code, instance);
    }
    return instance;
  }

  interface ErDiagramUiBinder extends UiBinder<Widget, ErDiagram> {
  }

  private static ErDiagramUiBinder uiBinder = GWT.create(ErDiagramUiBinder.class);

  /**
   * Mapper to convert VisNode to/from json string
   */
  interface VisNodeMapper extends ObjectMapper<List<VisNode>> {
  }

  /**
   * Mapper to convert VisEdge to/from json string
   */
  interface VisEdgeMapper extends ObjectMapper<List<JsniEdge>> {
  }

  private VisNodeMapper visNodeMapper = GWT.create(VisNodeMapper.class);

  private VisEdgeMapper visEdgeMapper = GWT.create(VisEdgeMapper.class);

  @UiField
  FlowPanel contentItems;

  private ErDiagram(final ViewerDatabase database, final ViewerSchema schema) {
    initWidget(uiBinder.createAndBindUi(this));

    SimplePanel config = new SimplePanel();
    config.getElement().setId("erconfig");
    contentItems.add(config);

    SimplePanel diagram = new SimplePanel();
    diagram.addStyleName("erdiagram");
    diagram.getElement().setId("erdiagram");
    diagram.addAttachHandler(new AttachEvent.Handler() {
      @Override
      public void onAttachOrDetach(AttachEvent event) {
        List<VisNode> visNodeList = new ArrayList<>();
        List<JsniEdge> jsniEdgeList = new ArrayList<>();

        int maxRows = 0;
        int maxRelationsIn = 0;
        int maxRelationsOut = 0;
        int maxRelationsTotal = 0;
        int maxColumns = 0;
        int maxColumnsAndRows = 0;

        int minRows = Integer.MAX_VALUE;
        int minRelationsIn = Integer.MAX_VALUE;
        int minRelationsOut = Integer.MAX_VALUE;
        int minRelationsTotal = Integer.MAX_VALUE;
        int minColumns = Integer.MAX_VALUE;
        int minColumnsAndRows = Integer.MAX_VALUE;

        int biggestSize = 0;
        for (ViewerTable viewerTable : schema.getTables()) {
          int size = getVisNodeSize(database, viewerTable);
          if (biggestSize < size) {
            biggestSize = size;
          }

          VisNode visNode = new VisNode(viewerTable.getUUID(), viewerTable.getName(), viewerTable.getDescription(),
            size);

          visNode.numColumns = viewerTable.getColumns().size();
          visNode.numRows = new Long(viewerTable.getCountRows()).intValue();
          visNode.numRelationsOut = viewerTable.getForeignKeys().size();
          int inboundForeignKeys = 0;
          for (ViewerSchema viewerSchema : database.getMetadata().getSchemas()) {
            for (ViewerTable table : viewerSchema.getTables()) {
              for (ViewerForeignKey viewerForeignKey : table.getForeignKeys()) {
                if (viewerForeignKey.getReferencedTableUUID().equals(viewerTable.getUUID())) {
                  inboundForeignKeys++;
                }
              }
            }
          }
          visNode.numRelationsIn = inboundForeignKeys;
          visNode.numRelationsTotal = visNode.numRelationsIn + visNode.numRelationsOut;
          visNode.numColumnsAndRows = visNode.numColumns * visNode.numRows;

          if (maxColumns < visNode.numColumns) {
            maxColumns = visNode.numColumns;
          }
          if (maxRows < visNode.numRows) {
            maxRows = visNode.numRows;
          }
          if (maxRelationsOut < visNode.numRelationsOut) {
            maxRelationsOut = visNode.numRelationsOut;
          }
          if (maxRelationsIn < visNode.numRelationsIn) {
            maxRelationsIn = visNode.numRelationsIn;
          }
          if (maxRelationsTotal < visNode.numRelationsTotal) {
            maxRelationsTotal = visNode.numRelationsTotal;
          }
          if (maxColumnsAndRows < visNode.numColumnsAndRows) {
            maxColumnsAndRows = visNode.numColumnsAndRows;
          }

          if (minColumns > visNode.numColumns) {
            minColumns = visNode.numColumns;
          }
          if (minRows > visNode.numRows) {
            minRows = visNode.numRows;
          }
          if (minRelationsOut > visNode.numRelationsOut) {
            minRelationsOut = visNode.numRelationsOut;
          }
          if (minRelationsIn > visNode.numRelationsIn) {
            minRelationsIn = visNode.numRelationsIn;
          }
          if (minRelationsTotal > visNode.numRelationsTotal) {
            minRelationsTotal = visNode.numRelationsTotal;
          }
          if (minColumnsAndRows > visNode.numColumnsAndRows) {
            minColumnsAndRows = visNode.numColumnsAndRows;
          }

          visNodeList.add(visNode);

          for (ViewerForeignKey viewerForeignKey : viewerTable.getForeignKeys()) {
            jsniEdgeList.add(new JsniEdge(viewerTable.getUUID(), viewerForeignKey.getReferencedTableUUID()));
          }
        }

        for (VisNode visNode : visNodeList) {
          visNode
            .adjustSize(getNormalizedValue(visNode.numRelationsTotal, minRelationsTotal, maxRelationsTotal, 10, 50));

          visNode.adjustBackgroundColor(((double) getNormalizedValue(Math.log(visNode.numColumnsAndRows),
            Math.log(minColumnsAndRows), Math.log(maxColumnsAndRows), 1, 30)) / 100);
        }

        String nodes = visNodeMapper.write(visNodeList);
        String edges = visEdgeMapper.write(jsniEdgeList);

        loadDiagram(nodes, edges);
      }
    });
    contentItems.add(diagram);
  }

  private int getNormalizedValue(double value, double min, double max, double minNorm, double maxNorm) {
    double range = (double) max - min;
    double zeroToOne = (value - min) / range;

    double range2 = (double) maxNorm - minNorm;
    Double result = (zeroToOne * range2) + minNorm;
    return result.intValue();
  }

  private int getVisNodeSize(ViewerDatabase database, ViewerTable table) {
    int inboundForeignKeys = 0;
    for (ViewerSchema viewerSchema : database.getMetadata().getSchemas()) {
      for (ViewerTable viewerTable : viewerSchema.getTables()) {
        for (ViewerForeignKey viewerForeignKey : viewerTable.getForeignKeys()) {
          if (viewerForeignKey.getReferencedTableUUID().equals(table.getUUID())) {
            inboundForeignKeys++;
          }
        }
      }
    }

    // assign a weight to the table node
    Double weight = Math.log(table.getCountRows() * table.getColumns().size()) + 1.5 * table.getForeignKeys().size()
      + inboundForeignKeys;

    return weight.intValue();
  }

  /**
   * vis.js graph node object.
   */
  static class VisNode {
    String id;
    String label;
    VisNodeFont font;
    String shape;
    int size;
    String title;
    VisNodeColor color;

    int numRows;
    int numRelationsIn;
    int numRelationsOut;
    int numRelationsTotal;
    int numColumns;
    int numColumnsAndRows;

    public VisNode(String id, String label, String title, int size) {
      this.id = id;
      this.label = label;
      this.font = new VisNodeFont(25);
      this.size = size;
      this.title = title;
      this.color = new VisNodeColor();
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public VisNodeFont getFont() {
      return font;
    }

    public void setFont(VisNodeFont font) {
      this.font = font;
    }

    public String getShape() {
      return shape;
    }

    public void setShape(String shape) {
      this.shape = shape;
    }

    public int getSize() {
      return size;
    }

    public void setSize(int size) {
      this.size = size;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public VisNodeColor getColor() {
      return color;
    }

    public void setColor(VisNodeColor color) {
      this.color = color;
    }

    public void adjustSize(int fontSize) {
      this.setSize(fontSize);
    }

    public void adjustBackgroundColor(double value) {
      // this.color.background = "#" + hslToRgb(0.59722222222, 1.0, 0.91);

      GWT.log("using " + (0.91 - value));

      this.color.background = "#" + hslToRgb(0.59722222222, 1.0, 0.91 - value);
    }

    /**
     * Converts an HSL color value to RGB. Conversion formula adapted from
     * http://en.wikipedia.org/wiki/HSL_color_space. Assumes h, s, and l are
     * contained in the set [0, 1] and returns r, g, and b in the set [0, 255].
     *
     * source: http://stackoverflow.com/a/29316972/1483200
     *
     * @param h
     *          The hue
     * @param s
     *          The saturation
     * @param l
     *          The lightness
     * @return int array, the RGB representation
     */
    public static String hslToRgb(double h, double s, double l) {
      double r, g, b;

      if (s == 0f) {
        r = g = b = l; // achromatic
      } else {
        double q = l < 0.5f ? l * (1 + s) : l + s - l * s;
        double p = 2 * l - q;
        r = hueToRgb(p, q, h + 1f / 3f);
        g = hueToRgb(p, q, h);
        b = hueToRgb(p, q, h - 1f / 3f);
      }
      String rs = Integer.toHexString((int) (r * 255));
      String gs = Integer.toHexString((int) (g * 255));
      String bs = Integer.toHexString((int) (b * 255));
      return rs + gs + bs;
    }

    /**
     * Helper method that converts hue to rgb, source:
     * http://stackoverflow.com/a/29316972/1483200
     */
    public static double hueToRgb(double p, double q, double t) {
      if (t < 0f)
        t += 1f;
      if (t > 1f)
        t -= 1f;
      if (t < 1f / 6f)
        return p + (q - p) * 6f * t;
      if (t < 1f / 2f)
        return q;
      if (t < 2f / 3f)
        return p + (q - p) * (2f / 3f - t) * 6f;
      return p;
    }

    /**
     * vis.js graph node font object.
     */
    static class VisNodeFont {
      int size;

      public VisNodeFont(int size) {
        this.size = size;
      }

      public int getSize() {
        return size;
      }

      public void setSize(int size) {
        this.size = size;
      }
    }

    static class VisNodeColor {
      String background;
      String border;

      public VisNodeColor() {
        this.background = "#D2E5FF";
        this.border = "#2B7CE9";
      }

      public String getBackground() {
        return background;
      }

      public void setBackground(String background) {
        this.background = background;
      }

      public String getBorder() {
        return border;
      }

      public void setBorder(String border) {
        this.border = border;
      }
    }
  }

  /**
   * vis.js graph edge object.
   */
  static class JsniEdge {
    String from;
    String to;
    String arrows;
    int length;

    public JsniEdge(String from, String to) {
      this.from = from;
      this.to = to;
      this.length = 100;
    }

    public String getFrom() {
      return from;
    }

    public void setFrom(String from) {
      this.from = from;
    }

    public String getTo() {
      return to;
    }

    public void setTo(String to) {
      this.to = to;
    }

    public String getArrows() {
      return arrows;
    }

    public void setArrows(String arrows) {
      this.arrows = arrows;
    }

    public int getLength() {
      return length;
    }

    public void setLength(int length) {
      this.length = length;
    }
  }

  public static native void loadDiagram(String nodesJson, String edgesJson)
  /*-{
    (function erdiagramload(){

        // create an array with nodes
        var nodes = new $wnd.vis.DataSet(eval(nodesJson));

        console.log(eval(nodesJson));

        // create an array with edges
        var edges = new $wnd.vis.DataSet(eval(edgesJson));

        // create a network
        var container = $wnd.document.getElementById('erdiagram');

        // provide the data in the vis format
        var data = {
          nodes: nodes,
          edges: edges
        };
        var options = {
            "nodes": {
                "font": {
                    "strokeWidth": 0,
                    "face": "Ubuntu, HelveticaNeue, Helvetica Neue, Helvetica, Arial, sans-serif"
                },
                "shape": "dot",
                scaling: {
                    min: 15,
                    max: 50,
                    label: {
                        enabled: true,
                        min: 15,
                        max: 50,
                        maxVisible: 60,
                        drawThreshold: 5
                    }
                },
                "color": {
                    "hover": {
                        "border": "#a19b4b",
                        "background": "#f2f0a1"
                    },
                    "highlight": {
                        "border": "#948e3e",
                        "background": "#e5e394"
                    }
                }
            },
            "edges": {
                "smooth": {
                    "type": "straightCross",
                    "forceDirection": "horizontal"
                }, "arrows": {
                    "to": {
                        "enabled": true
                    }
                }
            },
            "interaction": {
                "hover": true,
                "navigationButtons": true,
                "zoomView": false
            },
            "physics": {
                "enabled": true,
                "solver": "repulsion",
                "repulsion": {
                    "centralGravity": 0.1,
                    "springLength": 25,
                    "springConstant": 0,
                    "nodeDistance": 150,
                    "damping": 0.09
                },
            }//, configure: {enabled: true, showButton: true, container: $wnd.document.getElementById('erconfig')}
        };



        // initialize your network!
        var network = new $wnd.vis.Network(container, data, options);
    })();
  }-*/;
}
