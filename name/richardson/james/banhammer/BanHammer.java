/*******************************************************************************
 * Copyright (c) 2011 James Richardson.
 * 
 * BanHammer.java is part of BanHammer.
 * 
 * BanHammer is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation, either version 3 of the License, or (at your option) 
 * any later version.
 * 
 * BanHammer is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with BanHammer.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package name.richardson.james.banhammer;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.persistence.PersistenceException;

import name.richardson.james.banhammer.ban.BanCommand;
import name.richardson.james.banhammer.ban.BanHandler;
import name.richardson.james.banhammer.ban.BanRecord;
import name.richardson.james.banhammer.ban.CheckCommand;
import name.richardson.james.banhammer.ban.ExportCommand;
import name.richardson.james.banhammer.ban.HistoryCommand;
import name.richardson.james.banhammer.ban.ImportCommand;
import name.richardson.james.banhammer.ban.PardonCommand;
import name.richardson.james.banhammer.ban.PlayerListener;
import name.richardson.james.banhammer.ban.PurgeCommand;
import name.richardson.james.banhammer.ban.RecentCommand;
import name.richardson.james.banhammer.ban.ReloadCommand;
import name.richardson.james.banhammer.kick.KickCommand;
import name.richardson.james.banhammer.util.BanHammerTime;
import name.richardson.james.bukkit.dimensiondoor.DimensionDoorConfiguration;
import name.richardson.james.bukkit.util.Logger;
import name.richardson.james.bukkit.util.Plugin;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

public class BanHammer extends Plugin {

  private long maximumTemporaryBan;
  private static ResourceBundle messages;
  private final static Locale locale = Locale.getDefault();
  private final CommandManager cm;
  
  private PlayerListener playerListener;
  private PluginDescriptionFile desc;
  private PluginManager pm;
  private BanHammerConfiguration configuration;

  public BanHammer() {
    this.cm = new CommandManager();
  }

  /**
   * This returns a localised string from the loaded ResourceBundle.
   * 
   * @param key The key for the desired string.
   * @return The string for the given key.
   */
  public static String getMessage(String key) {
    return messages.getString(key);
  }
  
  @Override
  public List<Class<?>> getDatabaseClasses() {
    List<Class<?>> list = new ArrayList<Class<?>>();
    list.add(BanRecord.class);
    return list;
  }

  /**
   * This returns a handler to allow access to the BanHammer API.
   * 
   * @return A new BanHandler instance.
   */
  public BanHandler getHandler() {
    return new BanHandler(this.getServer());
  }

  @Override
  public void onDisable() {
    Logger.info(String.format(messages.getString("plugin-disabled"), this.desc.getName()));
  }

  @Override
  public void onEnable() {
    this.desc = this.getDescription();
    this.pm = this.getServer().getPluginManager();

    try {
      this.logger.setPrefix("[BanHammer] ");
      this.loadConfiguration();
      this.setupDatabase();
      this.setPermission();
      this.registerListeners();
      this.registerCommands();
    } catch (final IOException exception) {
      this.logger.severe("Unable to load configuration!");
      exception.printStackTrace();
    } catch (SQLException exception) {
      // TODO Auto-generated catch block
      exception.printStackTrace();
    } finally {
      if (!this.getServer().getPluginManager().isPluginEnabled(this)) return;
    }

    logger.info(String.format(BanHammer.getMessage("plugin-enabled"), this.desc.getFullName()));
  }

  private void loadConfiguration() throws IOException {
    configuration = new BanHammerConfiguration(this);
    if (configuration.isDebugging()) {
      Logger.enableDebugging(this.getDescription().getName().toLowerCase());
    }
  }

  private void setupCommands() {
    this.getCommand("ban").setExecutor(new BanCommand(this));
    this.getCommand("kick").setExecutor(new KickCommand(this));
    this.getCommand("pardon").setExecutor(new PardonCommand(this));
    this.getCommand("bh").setExecutor(this.cm);
    this.cm.registerCommand("check", new CheckCommand(this));
    this.cm.registerCommand("export", new ExportCommand(this));
    this.cm.registerCommand("history", new HistoryCommand(this));
    this.cm.registerCommand("import", new ImportCommand(this));
    this.cm.registerCommand("purge", new PurgeCommand(this));
    this.cm.registerCommand("recent", new RecentCommand(this));
    this.cm.registerCommand("reload", new ReloadCommand(this));
  }

  private void setupDatabase() {
    try {
      this.getDatabase().find(BanRecord.class).findRowCount();
    } catch (PersistenceException ex) {
      Logger.warning(BanHammer.getMessage("no-database"));
      this.installDDL();
    }
    BanRecord.setDatabase(this.getDatabase());
  }

  private void setupListeners() {
    this.playerListener = new PlayerListener();
    this.pm.registerEvent(Event.Type.PLAYER_LOGIN, this.playerListener, Event.Priority.Highest, this);
  }

  private void setupLocalisation() {
    BanHammer.messages = ResourceBundle.getBundle("name.richardson.james.banhammer.localisation.Messages", locale);
  }

  public long getMaximumTemporaryBan() {
    return maximumTemporaryBan;
  }

  public void setMaximumTemporaryBan(long maximumTemporaryBan) {
    this.maximumTemporaryBan = maximumTemporaryBan;
  }

}
