package com.zgamelogic.bot.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent;

import java.util.ArrayList;
import java.util.List;

@DiscordController
public class ForumBot {
    private final long TAG_ID = 1355163951289335840L;
    private final long FORUM_CHANNEL_ID = 1355163893957267477L;

    @DiscordMapping
    private void postTagsUpdatedEvent(ChannelUpdateAppliedTagsEvent event){
        event.getRemovedTags().forEach(tag -> {
            System.out.println(tag.getName());
        });
    }

    @DiscordMapping
    private void postChannelCreatedEvent(ChannelCreateEvent event){
        if(event.getChannelType() != ChannelType.GUILD_PUBLIC_THREAD) return;
        ForumChannel forumChannel = event.getChannel().asThreadChannel().getParentChannel().asForumChannel();
        if(forumChannel.getIdLong() != FORUM_CHANNEL_ID) return;
        ThreadChannel channel = event.getChannel().asThreadChannel();
        List<ForumTag> tags = new ArrayList<>(channel.getAppliedTags());
        tags.add(forumChannel.getAvailableTagById(TAG_ID));
        channel.getManager().setAppliedTags(tags).queue();
    }
}
