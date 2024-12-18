package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

public class ProjectDao extends DaoBase 
{
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";
	
	public void executeBatch(List<String> sqlBatch)
	{
		try(Connection conn = DbConnection.getConnection())
		{
			startTransaction(conn);
			
			try(Statement stat = conn.createStatement())
			{
				for(String sql : sqlBatch)
				{
					stat.addBatch(sql);
				}
				
				stat.executeBatch();
				System.out.println("Successfull, commiting");
				commitTransaction(conn);
				
			}
			catch (Exception e)
			{
				System.out.println("Failed, so Rolledback");
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}catch (SQLException e)
		{
			System.out.println("Failed connection while applying");
			throw new DbException(e);
		}
	}

	public Project insertProject(Project project) 
	{
		// @formatter:off 
		String sql = ""
		+ "INSERT INTO " + PROJECT_TABLE + " "
		+ "(project_name, estimated_hours, actual_hours, difficulty, notes)"
		+ "VALUES "
		+ "(?, ?, ?, ?, ?)";
		// @formatter:on
		try(Connection conn = DbConnection.getConnection())
		{
			startTransaction(conn);
			
			try(PreparedStatement stat = conn.prepareStatement(sql))
			{
				setParameter(stat, 1, project.getProjectName(), String.class); 
				setParameter(stat, 2, project.getEstimatedHours (), BigDecimal.class); 
				setParameter(stat, 3, project.getActualHours(), BigDecimal.class); 
				setParameter(stat, 4, project.getDifficulty(), Integer.class); 
				setParameter(stat, 5, project.getNotes (), String.class);
				
				stat.executeUpdate();
				Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
				commitTransaction(conn);
				project.setProjectId(projectId);
				return project;
				
			}
			catch(Exception e)
			{
				System.out.println("Failed in ProjectDao.inserProject");
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch (SQLException e)
		{
			System.out.println("Failed connection in ProjectDao.inserProject");
			throw new DbException(e);
		}
	}

	
	public List<Project> grabAllThoseProjects() 
	{
				
		//make formatted string for select statement
		// @formatter:off 
		String sql = ""
		+ "SELECT * "
		+ "FROM " + PROJECT_TABLE + " "
		+ "ORDER BY project_id";
		// @formatter:on
				
		
		//attempts connection
		try(Connection conn = DbConnection.getConnection())
		{
			//starts transaction
			startTransaction(conn);
						
			//try and prep statement
			try(PreparedStatement stat = conn.prepareStatement(sql))
			{		
				//runs a select statement and sends it over and adds the selected project to list
				//stat.executeUpdate();	
				try(ResultSet rs = stat.executeQuery())
				{
					List<Project> projects = new LinkedList<Project>();
					//adds objects info to list
					while(rs.next()) 
					{
						projects.add(extract(rs, Project.class));
					}
						
					//return the full list once completed.
					return projects;
				}
				catch(Exception e)
				{
					System.out.println("\n rs failure in 'grabAllThoseProjects'");
					throw new DbException(e);
				}
			}
			catch(Exception e)
			{
				System.out.println("\n select statement failure 'grabAllThoseProjects'");
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch (SQLException e)
		{
			System.out.println("\n connection failure 'grabAllThoseProjects'");
			throw new DbException(e);
		}
					
	}

	public Optional<Project> grabSpecificProject(Integer projectIdNumber) 
	{
		//sql command a bit different, this time using the ? switcher
		// @formatter:off 
		String sql = ""
		+ "SELECT * "
		+ "FROM " + PROJECT_TABLE + " "
		+ "WHERE project_id = ?";
		// @formatter:on
		
		//tryCatch into connection/transaction
		try(Connection conn = DbConnection.getConnection())
		{
			startTransaction(conn);
			
			try
			{
				//creates null project object
				Project project = null;
				
				//tests the prepared statement from above
				try(PreparedStatement stat = conn.prepareStatement(sql))
				{
					//sets the ? in the statement to the needed projectId
					setParameter(stat, 1, projectIdNumber, Integer.class);
					
					//I believe this is part one to grab info in a way that it can be stored in the object.
					try(ResultSet rs = stat.executeQuery())
					{
						if(rs.next())
						{
							//this is the second part.
							project = extract(rs, Project.class);
						}
					}
				}
				//grabs additional info if it isnt null
				if(Objects.nonNull(project))
				{
					//this is new, but it just grabs any additional info inside theproject (steps, cate, mats)
					project.getMaterials().addAll(grabMaterialsForProject(conn, projectIdNumber));
					project.getSteps().addAll(grabStepsForProject(conn, projectIdNumber));
					project.getCategories().addAll(grabCategoriesForProject(conn, projectIdNumber));
				}
				//sends the transaction
				commitTransaction(conn);
				
				// returns the object, even if null
				return Optional.ofNullable(project);
			}
			catch(Exception e)
			{
				System.out.println("\n select statement failure 'grabSpecificProject'");
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e)
		{
			System.out.println("\n connection failure 'grabSpecificProject'");
			throw new DbException(e);
		}
		//return null;
	}

	private List<Step> grabStepsForProject(Connection conn, Integer projectIdNumber) throws SQLException
	{
		// @formatter:off 
		String sql = "" + "SELECT s.* FROM " + STEP_TABLE + " s "
		+ "WHERE project_id = ?";
		// @formatter:on
				
		try(PreparedStatement stat = conn.prepareStatement(sql))
		{
			//changes the ? to correct projectId
			setParameter(stat, 1, projectIdNumber, Integer.class);
					
			//tries to create a resultset for the prepared statement, 
			//then makes a linked list and populates it
			try(ResultSet rs = stat.executeQuery())
			{
				List<Step> step = new LinkedList<Step>();
						
				while(rs.next())
				{
					step.add(extract(rs, Step.class));
				}
				return step;
			}
		}
		catch(Exception e)
		{
			System.out.println("Steps failed, may be null");
			throw new DbException(e);
		}
	}

	private List<Category> grabCategoriesForProject(Connection conn, Integer projectIdNumber)throws SQLException
	{
		// @formatter:off 
		String sql = "" + "SELECT c.* FROM " + CATEGORY_TABLE + " c "
		+ "JOIN " + PROJECT_CATEGORY_TABLE 
		+ " pc USING (category_id) " 
		+ "WHERE project_id = ?";
		// @formatter:on
		
		try(PreparedStatement stat = conn.prepareStatement(sql))
		{
			//changes the ? to correct projectId
			setParameter(stat, 1, projectIdNumber, Integer.class);
					
			//tries to create a resultset for the prepared statement, 
			//then makes a linked list and populates it
			try(ResultSet rs = stat.executeQuery())
			{
				List<Category> cat = new LinkedList<Category>();
						
				while(rs.next())
				{
					cat.add(extract(rs, Category.class));
				}
				return cat;
			}
		}
	}

	private List<Material> grabMaterialsForProject(Connection conn, Integer projectIdNumber)throws SQLException
	{
		// @formatter:off 
		String sql = "" + "SELECT m.* FROM " + MATERIAL_TABLE + " m "
		//+ "JOIN " + PROJECT_CATEGORY_TABLE 
		//+ " pc USING (category_id) " 
		+ "WHERE project_id = ?";
		// @formatter:on
		
		try(PreparedStatement stat = conn.prepareStatement(sql))
		{
			//changes the ? to correct projectId
			setParameter(stat, 1, projectIdNumber, Integer.class);
			
			//tries to create a resultset for the prepared statement, 
			//then makes a linked list and populates it
			try(ResultSet rs = stat.executeQuery())
			{
				List<Material> mat = new LinkedList<Material>();
				
				while(rs.next())
				{
					mat.add(extract(rs, Material.class));
				}
				return mat;
			}
		}
	}

	public boolean modifyProjectDetails(Project project) 
	{
		// @formatter:off 
			String sql = ""
			+ "UPDATE " + PROJECT_TABLE + " SET " 
			+ "project_name = ?, "
			+ "estimated_hours = ?, "
			+ "actual_hours = ?, "
			+ "difficulty = ?, "
			+ "notes = ? "
			+ "WHERE project_id = ?"; 
		// @formatter:on
			
			try(Connection conn = DbConnection.getConnection())
			{
				startTransaction(conn);
				
				try(PreparedStatement stat = conn.prepareStatement(sql))
				{
					setParameter(stat, 1, project.getProjectName(), String.class); 
					setParameter(stat, 2, project.getEstimatedHours (), BigDecimal.class); 
					setParameter(stat, 3, project.getActualHours(), BigDecimal.class); 
					setParameter(stat, 4, project.getDifficulty(), Integer.class); 
					setParameter(stat, 5, project.getNotes(), String.class);
					setParameter(stat, 6, project.getProjectId(), Integer.class);
					
					//creates boolean that should return true if the update runs correctly
					boolean success = stat.executeUpdate() == 1;
					
					commitTransaction(conn);
					return success;
					
				}
				catch(Exception e)
				{
					System.out.println("Failed in ProjectDao.modifyProjectDetails");
					rollbackTransaction(conn);
					throw new DbException(e);
				}
			}
			catch (SQLException e)
			{
				System.out.println("Failed connection in ProjectDao.modifyProjectDetails");
				throw new DbException(e);
			}
	}
	

	public boolean removeProject(Project project) 
	{
			//wrote two different statements for deleting the category and project separately.
		// @formatter:off 
			String sqlRemoveCategory = "DELETE FROM projects." + CATEGORY_TABLE  
            + " WHERE projects.category.category_id = ?;";
			String sqlRemoveProject = "DELETE FROM projects." + PROJECT_TABLE  
                    + " WHERE projects.project.project_id = ?;";
		// @formatter:on
			
		try(Connection conn = DbConnection.getConnection())
		{
			//single connection can be used to do multiple statements
			startTransaction(conn);
						
			try(PreparedStatement statCategory = conn.prepareStatement(sqlRemoveCategory); PreparedStatement statProject = conn.prepareStatement(sqlRemoveProject))
			{
				//Since Category didnt have a foreign key, I couldnt use ON DELETE CASCADE so I split them into two separate spots
				//This one sets the parameters for the preparedstatement for category
				setParameter(statCategory, 1, project.getProjectId(), Integer.class);
				
				//This one sets the parameters for the preparedstatement for Project
				setParameter(statProject, 1, project.getProjectId(), Integer.class);	
				
				//creates boolean that should return true if both category and project update correctly.
				boolean success = (statCategory.executeUpdate() == 1) && (statProject.executeUpdate() == 1);	
				
				commitTransaction(conn);
				return success;
							
			}
			catch(Exception e)
			{
				System.out.println("Failed in ProjectDao.removeProject");
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch (SQLException e)
		{
			System.out.println("Failed connection in ProjectDao.removeProject");
			throw new DbException(e);
		}
	}
}
