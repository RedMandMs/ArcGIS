package ru.lenoblgis.sergey.tests.arcgis.webmapapp;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import com.esri.core.portal.Portal;
import com.esri.core.portal.WebMap;
import com.esri.map.ArcGISFeatureLayer;
import com.esri.map.GroupLayer;
import com.esri.map.JMap;
import com.esri.map.Layer;
import com.esri.map.LayerEvent;
import com.esri.map.LayerListEventListenerAdapter;
import com.esri.map.MapEvent;
import com.esri.map.MapEventListenerAdapter;
import com.esri.toolkit.bookmarks.JExtentBookmark;
import com.esri.toolkit.overlays.InfoPopupOverlay;

public class Main {

	JMap map = new JMap();
	
	// create the extent bookmark UI component
	JExtentBookmark extentBookmarks = new JExtentBookmark(map);
	
	final InfoPopupOverlay popupOverlay = new InfoPopupOverlay();
	
	WebMap webMap = null;
	
	public static void main(String[] args) {
		Main main = new Main();
		main.go();
	}

	private void go(){
		JFrame window = new JFrame();
		window.setSize(800, 600);
		window.setLocationRelativeTo(null); // center on screen
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().setLayout(new BorderLayout());
		window.setVisible(true);
		
		window.getContentPane().add(map, BorderLayout.CENTER);
		window.getContentPane().add(extentBookmarks, BorderLayout.WEST);
		
		// ArcGIS Online portal, anonymous access
		Portal portal = new Portal("https://www.arcgis.com", null);
		// web map item ID on the above portal
		String itemID = "095e264aea1040238de556c923991cd3";
		
		// add overlay to map
		map.addMapOverlay(popupOverlay);
		
		try {
		  // create the WebMap instance
		  webMap = WebMap.newInstance(itemID, portal);
		  
		  map.getLayers().addLayerListEventListener(new LayerListEventListenerAdapter() {
		      
			  @Override
			  public void multipleLayersAdded(LayerEvent event) {
			    for (Layer layer : event.getChangedLayers().values()) {
			      if (layer instanceof ArcGISFeatureLayer) {
			        popupOverlay.addLayer(layer);
			      } else if (layer instanceof GroupLayer) {
			        for (Layer groupedLayer: ((GroupLayer) layer).getLayers()) {
			          if (groupedLayer instanceof ArcGISFeatureLayer) {
			            popupOverlay.addLayer(groupedLayer);
			          }
			        }
			      }
			    }
			  }
			      
			  @Override
			  public void layerAdded(LayerEvent event) {
			    Layer layer = event.getChangedLayer();
			    if (layer instanceof ArcGISFeatureLayer) {
			      popupOverlay.addLayer(layer);
			    } else if (layer instanceof GroupLayer) {
			      for (Layer groupedLayer: ((GroupLayer) layer).getLayers()) {
			        if (groupedLayer instanceof ArcGISFeatureLayer) {
			          popupOverlay.addLayer(groupedLayer);
			        }
			      }
			    }
			  }
			});
		  
		  map.addMapEventListener(new MapEventListenerAdapter() {
			  @Override
			  public void mapReady(MapEvent event) {
			    extentBookmarks.addBookmarks(webMap.getBookmarks());
			  }
			});
		  
		// load the WebMap into the JMap
		  map.loadWebMap(webMap);
		} catch (Exception e) {
		  // handle any exception / display to the user
		}
	}
}
