package ru.lenoblgis.sergey.tests.arcgis.geocoding;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.esri.core.geometry.Envelope;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;
import com.esri.map.GraphicsLayer;
import com.esri.map.JMap;
import com.esri.map.MapOptions;
import com.esri.map.MapOptions.MapType;

public class PlaceSearch {

	// geocode service
	private static final String GEOCODE_URL = 
	  "http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer";
	// text for geocode label 
	private static final String GEOCODE_LABEL = "Enter a USA input address:"; 
	// locate button string
	private static final String BUTTON_LOCATE = "Locate address on map";
	// address hint
	private static final String ADDRESS = "380 New York St Redlands CA 92373";
	// red pin marker PNG image hosted by ArcGIS
	private static final String SYMBOL_URL = 
	  "http://guitarbloknot.ru/wp-content/uploads/2014/08/86133.png";
	
	
	private GraphicsLayer locationLayer;
	private PictureMarkerSymbol locationSymbol;
	private Locator locator;
	
	
	//UI components
	// label with instructions
	private JLabel geocodeLabel;
	// text field to get user input
	private JTextField addressInput;
	
	private JFrame window;
	private JMap map;

	public PlaceSearch() {
	  window = new JFrame();
	  window.setSize(800, 600);
	  window.setLocationRelativeTo(null); // center on screen
	  window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  window.getContentPane().setLayout(new BorderLayout(0, 0));

	  // dispose map just before application window is closed.
	  window.addWindowListener(new WindowAdapter() {
		  @Override
		  public void windowClosing(WindowEvent windowEvent) {
			  super.windowClosing(windowEvent);
        map.dispose();
		  }
	  });

	  // Before this application is deployed you must register the application on 
	  // http://developers.arcgis.com and set the Client ID in the application as shown 
	  // below. This will license your application to use Basic level functionality.
		//
		// If you need to license your application for Standard level
		// functionality, please
		// refer to the documentation on http://developers.arcgis.com
		//
		// ArcGISRuntime.setClientID("your Client ID");

		// Using MapOptions allows for a common online basemap to be chosen
		MapOptions mapOptions = new MapOptions(MapType.TOPO);
		map = new JMap(mapOptions);
		// set default extent to North America
		map.setExtent(new Envelope(-20042400, 856094, -2783530, 11716267));

		// create symbol
		locationSymbol = new PictureMarkerSymbol(SYMBOL_URL);
		locationSymbol.setSize(40, 40);
		// Y-offset of half the height so that bottom of the pin points to
		// location
		locationSymbol.setOffsetY(20.0f);
		// If you don't use MapOptions, use the empty JMap constructor and add a
		// tiled layer
		// map = new JMap();
		// ArcGISTiledMapServiceLayer tiledLayer = new
		// ArcGISTiledMapServiceLayer(
		// "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");
		// map.getLayers().add(tiledLayer);

		// Add the JMap to the JFrame's content pane
		JLayeredPane contentPane = new JLayeredPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(createPanel());
		contentPane.add(map);
		window.add(contentPane);

		locationLayer = new GraphicsLayer();
		map.getLayers().add(locationLayer);

		// locator set up
		locator = Locator.createOnlineLocator(GEOCODE_URL);
	}

	private Component createPanel() {
		// user panel
		JPanel panel = new JPanel();
		panel.setLocation(10, 10);      
		panel.setBackground(Color.DARK_GRAY);
		panel.setBounds(10, 10, 320, 90);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));    
		 
		// address label
		geocodeLabel = new JLabel(GEOCODE_LABEL);
		geocodeLabel.setForeground(Color.WHITE);

		// user input text field
		addressInput = new JTextField();
		addressInput.setAlignmentX(Component.LEFT_ALIGNMENT);
		addressInput.setText(ADDRESS);
		 
		// geocode button
		JButton findButton = new JButton(BUTTON_LOCATE);
		findButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		findButton.addActionListener(new ActionListener() {
			  @Override
			  public void actionPerformed(ActionEvent e) {
			    // remove any previous graphics
			    locationLayer.removeAll();
			    // get address text from text field and use it to geocode
			    LocatorGeocodeResult result = geocode(addressInput.getText());
			    displayGeocodeResult(result);
			  }
			});
		 
		// add the UI components to your panel
		panel.add(geocodeLabel);
		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		panel.add(addressInput);
		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		panel.add(findButton);
		panel.setVisible(true);
		return panel;
	}
	
	private void displayGeocodeResult(LocatorGeocodeResult result) {
		if (result == null) return;
		// make sure this runs on the UI thread
		SwingUtilities.invokeLater(new Runnable() {

		  @Override
		  public void run() {
		    // create and populate attribute map
		    Map<String, Object> attributes = new HashMap<String, Object>();
		    for (Entry<String, String> entry : result.getAttributes().entrySet()) {
		      attributes.put(entry.getKey(), entry.getValue());
		    }
		    // create a graphic at this location
		    Graphic addressGraphic = new Graphic(result.getLocation(), locationSymbol, attributes);
		    locationLayer.addGraphic(addressGraphic);

		    // centre the map at this location, in a 10x10 km envelope
		    Envelope extent = map.getExtent();
		    extent.centerAt(result.getLocation(), 10000, 10000);
		    map.zoomTo(extent);
		  }
		});
	}
	
	private LocatorGeocodeResult geocode(String text) {
		// Set basic parameters to locate the address
		LocatorFindParameters findParams = new LocatorFindParameters(text);
		findParams.setOutSR(map.getSpatialReference());
		findParams.setMaxLocations(4);
		LocatorGeocodeResult highestScoreResult = null;
		try {
		  List<LocatorGeocodeResult> results = locator.find(findParams);
		  if (results != null && results.size() > 0) 
		    highestScoreResult = results.get(0);
		  else {
		    // show no address found message
		    JOptionPane.showMessageDialog(map.getParent(), "No address found!");
		  }
		} catch (Exception ex) {
		  // show exception error message
		  JOptionPane.showMessageDialog(map.getParent(), ex.getMessage());
		  ex.printStackTrace();
		}
		return highestScoreResult;
	}

	/**
	 * Starting point of this application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					PlaceSearch application = new PlaceSearch();
					application.window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
