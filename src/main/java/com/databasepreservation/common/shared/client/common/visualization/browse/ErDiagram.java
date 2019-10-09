package com.databasepreservation.common.shared.client.common.visualization.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.ViewerStructure.ViewerForeignKey;
import com.databasepreservation.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.common.shared.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.shared.client.tools.ViewerStringUtils;
import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ErDiagram extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
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

  final String databaseUUID;

  private ErDiagram(final ViewerDatabase database, final ViewerSchema schema) {
    databaseUUID = database.getUUID();
    initWidget(uiBinder.createAndBindUi(this));

    contentItems.add(new HTMLPanel(
      CommonClientUtils.getFieldHTML(messages.diagram_usingTheDiagram(), messages.diagram_Explanation())));

    SimplePanel config = new SimplePanel();
    config.getElement().setId("erconfig");
    contentItems.add(config);

    final SimplePanel diagram = new SimplePanel();
    diagram.addStyleName("erdiagram");
    diagram.getElement().setId("erdiagram");
    diagram.addAttachHandler(new AttachEvent.Handler() {
      @Override
      public void onAttachOrDetach(AttachEvent event) {
        // avoid setting up the diagram more than once
        if (diagram.getStyleName().contains("initialized-erdiagram")) {
          return;
        }

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
        int minColumnsAndRowsBiggerThanZero = Integer.MAX_VALUE;

        for (ViewerTable viewerTable : schema.getTables()) {
          VisNode visNode = new VisNode(viewerTable.getUUID(), viewerTable.getName());

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
          if (minColumnsAndRowsBiggerThanZero > visNode.numColumnsAndRows && visNode.numColumnsAndRows > 0) {
            minColumnsAndRowsBiggerThanZero = visNode.numColumnsAndRows;
          }

          // create tooltip with table information
          StringBuilder tooltip = new StringBuilder();
          if (ViewerStringUtils.isNotBlank(viewerTable.getName())) {
            tooltip.append(viewerTable.getName()).append("<br/>");
          }
          tooltip.append(messages.diagram_rows(visNode.numRows)).append(", ")
            .append(messages.diagram_columns(visNode.numColumns)).append(", ")
            .append(messages.diagram_relations(visNode.numRelationsTotal)).append(".");

          visNode.setTitle(tooltip.toString());

          visNodeList.add(visNode);

          for (ViewerForeignKey viewerForeignKey : viewerTable.getForeignKeys()) {
            jsniEdgeList.add(new JsniEdge(viewerTable.getUUID(), viewerForeignKey.getReferencedTableUUID()));
          }
        }

        Double normMinColumnsAndRows = 0.0;
        Double normMaxColumnsAndRows = 2.0; // N

        for (VisNode visNode : visNodeList) {
          // visNode
          // .adjustSize(getNormalizedValue(visNode.numRelationsTotal,
          // minRelationsTotal, maxRelationsTotal, 10, 50));

          visNode.adjustBackgroundColor(
            getNormalizedValue(visNode.numRelationsTotal, minRelationsTotal, maxRelationsTotal, 0.01, 0.70));

          if (visNode.numColumnsAndRows == 0) {
            visNode.adjustSize(20);
          } else {
            Double normNumColumnsAndRows = getNormalizedValue(visNode.numColumnsAndRows,
              minColumnsAndRowsBiggerThanZero, maxColumnsAndRows, normMinColumnsAndRows, normMaxColumnsAndRows);

            Double normResult = Math.asin(normNumColumnsAndRows - 1) + 1.5;

            visNode.adjustSize(getNormalizedValue(normResult, 0, 3, 40, 150).intValue());
          }

          // Double logColumnsAndRows = Math.log(visNode.numColumnsAndRows);
          // if (logColumnsAndRows.isInfinite() || logColumnsAndRows.isNaN()) {
          // logColumnsAndRows = 0.0;
          // }
          // visNode
          // .adjustSize(getNormalizedValue(visNode.numColumnsAndRows,
          // minColumnsAndRows,
          // maxColumnsAndRows, 20, 150));

          // visNode.adjustBackgroundColor(((double)
          // getNormalizedValue(logColumnsAndRows, logMinColumnsAndRows,
          // logMaxColumnsAndRows, 1, 70)) / 100);
        }

        String nodes = visNodeMapper.write(visNodeList);
        String edges = visEdgeMapper.write(jsniEdgeList);

        loadDiagram(databaseUUID, nodes, edges, ApplicationType.getType());
      }
    });
    contentItems.add(diagram);
  }

  private Double getNormalizedValue(double value, double min, double max, double minNorm, double maxNorm) {
    if (max == min) {
      return minNorm;
    }
    double range = max - min;
    double zeroToOne = (value - min) / range;

    double range2 = maxNorm - minNorm;
    return (zeroToOne * range2) + minNorm;
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

    public VisNode(String id, String label) {
      this.id = id;
      this.label = label;
      this.font = new VisNodeFont(25);
      this.size = 20;
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
      GWT.log("------> " + this.color.background);
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

      if (s == 0.0) {
        r = g = b = l; // achromatic
      } else {
        double q = l < 0.5 ? l * (1 + s) : l + s - l * s;
        double p = 2 * l - q;
        r = hueToRgb(p, q, h + 1.0 / 3.0);
        g = hueToRgb(p, q, h);
        b = hueToRgb(p, q, h - 1.0 / 3.0);
      }
      String rs = Integer.toHexString((int) (r * 255));
      if (rs.length() == 1) {
        rs = "0" + rs;
      }
      String gs = Integer.toHexString((int) (g * 255));
      if (gs.length() == 1) {
        gs = "0" + gs;
      }
      String bs = Integer.toHexString((int) (b * 255));
      if (bs.length() == 1) {
        bs = "0" + bs;
      }
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

  public static native void loadDiagram(String dbuuid, String nodesJson, String edgesJson, String applicationType)
  /*-{
    (function erdiagramload(){
        // network container
        var container = $wnd.document.getElementById('erdiagram');
  
        // avoid setting up the diagram more than once
        container.className += ' initialized-erdiagram';
  
        // create an array with nodes
        var rawNodes = eval(nodesJson);
        var rawNodesLen = rawNodes.length;
        for(var i=0; i<rawNodesLen; i+=1){
            if(rawNodes[i].title == null || rawNodes[i].title.length === 0){
                delete rawNodes[i].title;
            }
        }
        var nodes = new $wnd.vis.DataSet(rawNodes);
  
        // create an array with edges
        var edges = new $wnd.vis.DataSet(eval(edgesJson));
  
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
  
        network.on("selectNode", function (params) {
            //params.event = "[original event]";
            //console.log(params);
  
            if(params.nodes.length == 1) {
                //console.log("go to db" + dbuuid + " and table " + params.nodes[0]);
                var tableuuid = params.nodes[0];
                network.unselectAll();
                if (applicationType === @com.databasepreservation.common.shared.ViewerConstants::DESKTOP) {
                    @com.databasepreservation.common.shared.client.tools.HistoryManager::gotoDesktopTable(Ljava/lang/String;Ljava/lang/String;)(dbuuid, tableuuid);
                } else {
                    @com.databasepreservation.common.shared.client.tools.HistoryManager::gotoTable(Ljava/lang/String;Ljava/lang/String;)(dbuuid, tableuuid);
                }
            }
        });
  
        network.on("stabilized", function (params) {
            if(params.iterations > 1){
                options.physics.enabled = false;
                network.setOptions(options);
            }
        });
  
        network.on("dragEnd", function (params) {
            network.unselectAll();
        });
    })();
  }-*/;
}
