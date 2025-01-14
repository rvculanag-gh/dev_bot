/*
 * MIT License
 *
 * Copyright (c) 2022 pitzzahh
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package tech.araopj.springpitzzahhbot.commands;

import tech.araopj.springpitzzahhbot.commands.slash_commands.SlashCommand;
import tech.araopj.springpitzzahhbot.commands.chat_commands.ChatCommand;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
@Configuration
public class CommandsConfig {

    @Value("${bot.commands.confessions.confess-command}")
    private String confessCommand;

    @Value("${bot.commands.member-updates.member-updates-command}")
    private String memberUpdatesCommand;

    @Value("${bot.commands.prefix}")
    private String prefix;

    @Value("${bot.commands.rules}")
    private String rulesCommand;

    @Bean
    public List<ChatCommand> getChatCommands() {
        return new ArrayList<>();
    }

    @Bean
    public Map<String, SlashCommand> getSlashCommands() {
        return new HashMap<>();
    }

}
