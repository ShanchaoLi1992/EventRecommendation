package db.mongodb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

//import static method from a certain class (in this case Filters)
import static com.mongodb.client.model.Filters.eq;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterClient;


public class MongoDBConnection implements DBConnection {
	private MongoClient mongoClient;
	private MongoDatabase db;

	
	public MongoDBConnection() {
		mongoClient = MongoClients.create();
		db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
	}
	@Override
	public void close() {
		// TODO Auto-generated method stub
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		// TODO Auto-generated method stub
		if (db == null) {
			return;
		}
		db.getCollection("users").updateOne(new Document("user_id", userId)
				,new Document("$push",new Document("favorite", new Document("$each", itemIds))));
		

	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		// TODO Auto-generated method stub
		if (db == null) {
			return;
		}
		db.getCollection("users").updateOne(new Document("user_id", userId)
				, new Document("$pullAll", new Document("favorite", itemIds)));

	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		// TODO Auto-generated method stub
		if (db == null) {
			return new HashSet<String>();
		}
		Set<String> favoriteItemIds = new HashSet<>();
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		if (iterable.first() != null && iterable.first().containsKey("favorite")) {
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) iterable.first().get("favorite");
			favoriteItemIds.addAll(list);
		}
		return favoriteItemIds;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		// TODO Auto-generated method stub
		if (db == null) {
			return new HashSet<Item>();
		}
		Set<Item> favoriteItems = new HashSet<>();
		Set<String> favoriteItemIds = getFavoriteItemIds(userId);
		for (String itemId : favoriteItemIds) {
			FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", itemId));
			if (iterable.first() != null) {
				ItemBuilder builder = new ItemBuilder();
				Document doc = iterable.first();
				builder.setItemId(doc.getString("item_id"));
				builder.setName(doc.getString("name"));
				builder.setAddress(doc.getString("address"));
				builder.setUrl(doc.getString("url"));
				builder.setImageUrl(doc.getString("image_url"));
				builder.setRating(doc.getDouble("rating"));
				builder.setDistance(doc.getDouble("distance"));
				builder.setCategories(getCategories(itemId));
				favoriteItems.add(builder.build());

				
			}		
			
		}
		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		// TODO Auto-generated method stub
		if (db == null) {
			return new HashSet<>();
		}
		Set<String> categories = new HashSet<>();
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", itemId));
		
		if (iterable.first() != null && iterable.first().containsKey("categories")) {
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) iterable.first().get("categories");
			categories.addAll(list);
		}

		return categories;

	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		
		TicketMasterClient ticketMasterClient = new TicketMasterClient();
	    List<Item> items = ticketMasterClient.search(lat, lon, term);
	
	    for(Item item : items) {
	    	saveItem(item);      
	    }
	    return items;
	}

	@Override
	public void saveItem(Item item) {
		if (db == null) {
			return;
		}
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", item.getItemId()));
		if (iterable.first() == null) {
			db.getCollection("items")
			.insertOne(new Document().append("item_id", item.getItemId()).append("distance", item.getDistance())
					.append("name", item.getName()).append("address", item.getAddress())
					.append("url", item.getUrl()).append("image_url", item.getImageUrl())
					.append("rating", item.getRating()).append("categories", item.getCategories()));
		}
		

	}

	@Override
	public String getFullname(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean registerUser(String userId, String password, String firstName, String lastName) {
		// TODO Auto-generated method stub
		return false;
	}
	/*public static void main(String[] args) {
		DBConnection conn = new MongoDBConnection();
		Set<String> favoriteItemIds = conn.getFavoriteItemIds("1111");
		System.out.println(favoriteItemIds.size());
		
	}*/

}
