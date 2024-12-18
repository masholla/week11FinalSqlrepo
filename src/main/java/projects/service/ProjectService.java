package projects.service;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;


public class ProjectService 
{
	private static final String SCHEMA_FILE = "project_schema.sql";
	
	private ProjectDao projectDao = new ProjectDao();
	
	
	/*public static void main(String[] args)
	{
		new ProjectService().createAndPopulateTables();
	}*/
	
	public void createAndPopulateTables()
	{
		loadFromFile(SCHEMA_FILE);
	}

	private void loadFromFile(String fileName) 
	{
		String content = readFileContent(fileName);
		List<String> sqlStatements = convertContentToSqlStatements(content);
		
		sqlStatements.forEach(Line -> System.out.println(Line));
		
		projectDao.executeBatch(sqlStatements);
	
	}
	
	private List<String> convertContentToSqlStatements(String content) 
	{
		content = removeComments(content);
		
		content = replaceWhitespaceWithSpace(content);
		
		return extractLinesFromContent(content);
		
	}

	private List<String> extractLinesFromContent(String content) 
	{
		List<String> list = new ArrayList<String>();
		
		while(!content.isEmpty())
		{
			int line = content.indexOf(";");
			
			if(line == -1)
			{
				if(!content.isBlank())
				{
					list.add(content);
				}
				
				content = "";
			}else
			{
			list.add(content.substring(0, line).trim());
			content = content.substring( line + 1);
			}
		}
		
		return list;
	}

	private String replaceWhitespaceWithSpace(String content) 
	{
		return content.replaceAll("\\s+", " ");
	}

	//I think this is wrong.
	private String removeComments(String content) 
	{
		//makes a stringbuilder = to content (the file)
		StringBuilder build  = new StringBuilder(content);
		int commentPosition = 0;
		
		//while 0 is = to the index of the nearest "-- " after position 0, or while its not equal to -1
		while((commentPosition = build.indexOf("-- ", commentPosition)) != -1)
		{
			//creates an int looking for the end of the line's position. \n is a return command which would "end the line"
			int endOfLinePosition = build.indexOf("\n", commentPosition + 1);
			
			//this should be the last one
			if(endOfLinePosition == -1)
			{
				//this line is wrong I think. its supposed to take line breaks and comments and replace them with nothing.
				//I think what its doing instead is deleting from pos 0 to end of line pos and deleting it.
				build.replace(commentPosition, endOfLinePosition + 1, "");
				
				//instead, maybe this?
				//build.replace(endOfLinePosition, endOfLinePosition + 1, "");
			}
		}
		
		
		return build.toString();
		
	}

	private String readFileContent(String fileName) 
	{
		try 
		{
			Path path = Paths.get(getClass().getClassLoader().getResource(fileName).toURI());
			return Files.readString(path);
		} catch (Exception e) 
		{
			throw new DbException(e);
		}
	}

	
	public Project addProject(Project project) 
	{
		return projectDao.insertProject(project);
	}

	public List<Project> grabAllThoseProjects() 
	{
		return projectDao.grabAllThoseProjects();
	}

	public Project grabSpecificProject(Integer projectIdNumber) 
	{
		return projectDao.grabSpecificProject(projectIdNumber).orElseThrow(
		() -> new NoSuchElementException("Project with project ID = " + projectIdNumber + " does not exist."));
	}

	public void modifyProjectDetails(Project project) 
	{
		if(!projectDao.modifyProjectDetails(project))
		{
			throw new DbException("Project with ID = " + project.getProjectId() + " does not exist.");
		}
	}

	public void removeProject(Project project) 
	{
		
		try
		{
			if(!projectDao.removeProject(project))
			{
				throw new DbException("Project with ID = " + project.getProjectId() + " does not exist to delete.");
			}else 
			{
				System.out.println("Successfully deleted selected project");
			}
		}
		catch (Exception e)
		{
			System.out.println("ProjectService.removeProject failure");
		}
		
	}
}
