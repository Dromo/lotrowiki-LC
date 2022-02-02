import java.io.File;
import java.util.Objects;

import org.apache.log4j.Logger;

import delta.common.utils.io.FileIO;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.items.Item;

/**
 * Get item definitions from DAT files.
 * @author DAM
 */
public class IconBuilder
{
  private static final Logger LOGGER=Logger.getLogger(IconBuilder.class);

  private static final int[] TYPES=
  {
    2097, // Activator (in-world items that activate things when clicked on)
    2814, // PackageItem
    799, // IWeapon
    798, // ITextItem (scrolls, misc. papers)
    797, // IShield
    796, // IItem
    795, // IClothing
    794, // GameplayContainer (chests, fields, resource nodes)
    804, // Milestone (in-world milestones, camp site fires)
    805, // RecipeItem
    802, // Jewelry
    3663, // ? (recipe books)
    803, // Key (keys, and by extension items that allow opening things)
    815, // Waypoint (doors, horse, misc items used to zone)
    1722, // DoorTemplate (doors, misc similar items)
    3924, // ? (epic battles promotion points bestowers)
    4178 // Carry-alls
  };

  private static final File ITEM_ICONS_DIR_OLD=new File("../icons").getAbsoluteFile();
  private static final int[] OVERLAY_FOR_TIER=
  {
    1091914756, 1091914773, 1091914770, 1091914772, 1091914776, // 1-5
    1091914767, 1091914762, 1091914765, 1091914774, 1091914766, // 6-10
    1092396132, 1092396316, 1092508824, 1092694659 // 11-14
  };

  private DataFacade _facade;
  private int _currentId;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public IconBuilder(DataFacade facade)
  {
    _facade=facade;
  }

  private boolean _debug=false;
  private Item load(int indexDataId)
  {
    Item item=null;
    int dbPropertiesId=indexDataId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      _currentId=indexDataId;
      _debug=(_currentId==1879000000);
      if (_debug)
      {
        FileIO.writeFile(new File(indexDataId+".props"),properties.dump().getBytes());
        System.out.println(properties.dump());
      }
      // Icon
      Integer iconId=(Integer)properties.getProperty("Icon_Layer_ImageDID");
      Integer backgroundIconId=(Integer)properties.getProperty("Icon_Layer_BackgroundDID");
      Integer shadowIconId=(Integer)properties.getProperty("Icon_Layer_ShadowDID");
      Integer underlayIconId=(Integer)properties.getProperty("Icon_Layer_UnderlayDID");
      String iconName=buildIconName(iconId,backgroundIconId,shadowIconId,underlayIconId);
      //loadIcon(iconId);
      //loadIcon(backgroundIconId);
      //loadIcon(shadowIconId);
      //loadIcon(underlayIconId);
      
      if ((iconId!=null) || (backgroundIconId!=null) || (shadowIconId!=null) || (underlayIconId!=null))
      {
        File iconFile=new File(ITEM_ICONS_DIR_OLD,iconName+".png").getAbsoluteFile();
        if (!iconFile.exists())
        {
          System.out.println(iconName);
          int[] imageIds=new int[4];
          imageIds[0]=(backgroundIconId!=null)?backgroundIconId.intValue():0;
          imageIds[1]=(underlayIconId!=null)?underlayIconId.intValue():0;
          imageIds[2]=(shadowIconId!=null)?shadowIconId.intValue():0;
          imageIds[3]=(iconId!=null)?iconId.intValue():0;
          DatIconsUtils.buildImageFile(_facade,imageIds,iconFile);
        }
      }
    }
    else
    {
      LOGGER.warn("Could not handle item ID="+indexDataId);
    }
    return item;
  }

  private String buildIconName(Integer iconId, Integer backgroundIconId, Integer shadowIconId, Integer underlayIconId)
  {
    String iconName=null;
    if ((iconId!=null) || (backgroundIconId!=null) || (shadowIconId!=null) || (underlayIconId!=null))
    {
      iconName=((iconId!=null)?iconId:"0")+"-"+((backgroundIconId!=null)?backgroundIconId:"0");
      if (((shadowIconId!=null) && (shadowIconId.intValue()!=0)) ||
          ((underlayIconId!=null) && (underlayIconId.intValue()!=0)))
      {
        iconName=iconName+"-"+((shadowIconId!=null)?shadowIconId:"0");
        if (!Objects.equals(shadowIconId,underlayIconId))
        {
          if ((underlayIconId!=null) && (underlayIconId.intValue()!=0))
          {
            iconName=iconName+"-"+underlayIconId;
          }
        }
      }
    }
    return iconName;
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      //int did=BufferUtils.getDoubleWordAt(data,0);
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      for(int i=0;i<TYPES.length;i++)
      {
        if (TYPES[i]==classDefIndex)
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Load items, legacies, passives, consumables.
   */
  public void doIt()
  {
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        load(id);
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new IconBuilder(facade).doIt();
    facade.dispose();
  }
}
