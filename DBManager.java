import java.sql.*;


public class DBManager {
	//datbase link
	private static final String URL = "jdbc:sqlite:data.db";
// function that connects to sqlite
	private static Connection connect() throws SQLException{
		return DriverManager.getConnection(URL); 
	}

	// create a table
	public static void createTable(){
		//create a table if it doess ont exist yet
		String sql = "CREATE TABLE IF NOT EXISTS users ( id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT NOT NULL, attended INTEGER , total INTEGER);";
// try connecting to the database and execute the commands to create a table:
		try (Connection conn = connect(); Statement s = conn.createStatement()){
			s.execute(sql);
			System.out.println("table users is ready. ");
		}
		catch(SQLException e){
			System.out.println("Error creating table" + e.getMessage());
		}
	}

	//insert data:
	public static void insert(String sub, int attended, int tot){
		//command in sql: the ? is a placeholder we use setstring to pass onto that placeholder later
		String sql = " INSERT INTO users(subject,attended,total) VALUES(?,?,?);";
		//try doing this command;
		try(Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)){ // we use prepared statement instead of statement because this is driven by user input and changes from time to time , this is paramerised 
			ps.setString(1,sub);
			ps.setString(2,String.valueOf(attended));
			ps.setString(3,String.valueOf(tot));
			ps.executeUpdate();
			System.out.println("Inserted subject: " + sub);
		}catch(SQLException e){
			System.out.println("Error inserting this field: " + e.getMessage());
		}
	}


	//update fields:
//	public static void update(
	

	public static void delete(String sub){
		//remove the row that match the subject name
		String sql = "DELETE FROM users WHERE subject = ?;";
		try(Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, sub);
			int rows = ps.executeUpdate(); // number of rows that were removed
			if(rows > 0){
				System.out.println("Deleted subject: " + sub);
			}else{
				System.out.println("No subject found with the name: " + sub);
			}
		}catch(SQLException e){
			System.out.println("Error deleting this field: " + e.getMessage());
		}
	}

   	//update fields
	public static void update(String sub, int attended, int tot){
		//find the row by subject name and replace its attended and total values
		String sql = "UPDATE users SET attended = ?, total = ? WHERE subject = ?;";
		try(Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setInt(1, attended);
			ps.setInt(2, tot);
			ps.setString(3, sub);
			int rows = ps.executeUpdate(); // number of rows that were changed
			if(rows > 0){
				System.out.println("Updated subject: " + sub);
			}else{
				System.out.println("No subject found with the name: " + sub);
			}
		}catch(SQLException e){
			System.out.println("Error updating this field: " + e.getMessage());
		}
	}
	//loader from tables:
	public static java.util.List<Object[]> loadAll(){
    java.util.List<Object[]> rows = new java.util.ArrayList<>();
    String sql = "SELECT subject, attended, total FROM users;";
    try(Connection conn = connect(); Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)){
        while(rs.next()){
            rows.add(new Object[]{ rs.getString("subject"), rs.getInt("attended"), rs.getInt("total") });
        }
    }catch(SQLException e){
        System.out.println("Error loading: " + e.getMessage());
    }
    return rows;
}

}



