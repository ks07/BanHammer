/*******************************************************************************
 * Copyright (c) 2012 James Richardson.
 * 
 * BanHammerConfiguration.java is part of BanHammer.
 * 
 * BanHammer is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * BanHammer is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * BanHammer. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package name.richardson.james.bukkit.banhammer;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import name.richardson.james.bukkit.utilities.configuration.PluginConfiguration;
import name.richardson.james.bukkit.utilities.formatters.TimeFormatter;

public class BanHammerConfiguration extends PluginConfiguration {

  /** The configured ban limits. */
  private final Map<String, Long> limits = new LinkedHashMap<String, Long>();

  /**
   * Instantiates a new BanHammer configuration.
   * 
   * @param plugin the plugin that this configuration belongs to.
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public BanHammerConfiguration(final BanHammer plugin) throws IOException {
    super(plugin);
    this.setBanLimits();
  }

  /**
   * Gets the ban limits.
   * 
   * @return the ban limits
   */
  public Map<String, Long> getBanLimits() {
    return Collections.unmodifiableMap(this.limits);
  }

  /**
   * Checks if is alias should be enabled.
   * 
   * @return true, if is alias is enabled
   */
  public boolean isAliasEnabled() {
    return this.getConfiguration().getBoolean("alias-plugin.enabled");
  }

  /**
   * Read and sets the ban limits.
   */
  public void setBanLimits() {
    this.limits.clear();
    final ConfigurationSection section = this.getConfiguration().getConfigurationSection("ban-limits");
    for (final String key : section.getKeys(false)) {
      try {
        final String name = key;
        final Long length = TimeFormatter.parseTime(section.getString(key));
        this.limits.put(name, length);
      } catch (final NumberFormatException e) {
        this.getLogger().warning(this, "limit-invalid", key);
      }
    }
  }
  
  public List<String> getImmunePlayers() {
    return this.getConfiguration().getStringList("immune-players");
  }

}
