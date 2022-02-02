import java.io.File;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DatConfiguration;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * DAT archive explorer.
 * @author DAM
 */
public class DATExplorerDrono
{
  private static final int OFFSET=DATConstants.DBPROPERTIES_OFFSET;

  private DataFacade _facade;

  private void doIt()
  {
	DatConfiguration config = new DatConfiguration();
	config.setRootPath(new File("./lotro")); //points to a symlink to lotro install folder
    _facade=new DataFacade(config);
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
    	showProperties(_facade,id+OFFSET);
    }
    _facade.dispose();
    _facade=null;
  }

  PropertiesSet showProperties(DataFacade facade, int id)
  {
    PropertiesSet properties=facade.loadProperties(id);
    if (properties!=null)
    {
      System.out.println("******** Properties: "+(id-OFFSET));
      System.out.println(properties.dump());
    }
    else
    {
      System.out.println("Properties "+id+" not found!");
    }
    return properties;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new DATExplorerDrono().doIt();
  }
}