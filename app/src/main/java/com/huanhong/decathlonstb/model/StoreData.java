package com.huanhong.decathlonstb.model;

import java.util.List;

public class StoreData {

	public List<City> city;
	public String zone;
	public String zone_code;

	public class City {
		public String city_name;
		public String city_id;
		public List<Store> shop;

		public class Store {
			public String shop_no;
			public String shop_name;
		}
	}
}
