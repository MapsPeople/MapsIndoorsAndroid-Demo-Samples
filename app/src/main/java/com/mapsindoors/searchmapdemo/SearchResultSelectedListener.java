package com.mapsindoors.searchmapdemo;

/**
 * <p>Listener interface to catch location search results.</p>
 * @author Martin Hansen
 */
public interface SearchResultSelectedListener {
	/**
	 * Listener method to catch search events from the menu.
	 */
	void onSearchResultSelected(Object searchResult);
}
