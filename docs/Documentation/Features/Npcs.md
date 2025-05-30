---
icon: material/message-text
tags:
  - Npcs
---

Npcs are an essential part of every RPG for player ingame interaction.
In BetonQuest Npcs can be used to start conversations or interact with them otherwise,
as shown in the `Scripting` and `Visual Effects` section of the documentation.

!!! info
    This Npc is not related to the NPC/Quester in [Conversations](Conversations.md)

## Provided Integrations

BetonQuest provides Integrations for the following Npc plugins:

- [Citizens](../Scripting/Building-Blocks/Integration-List.md#citizens)
- [FancyNpcs](../Scripting/Building-Blocks/Integration-List.md#fancynpcs)
- [ZNPCsPlus](../Scripting/Building-Blocks/Integration-List.md#znpcsplus)

## Referring an Npc

Npcs are defined in the `npcs` section.
For exact instruction definition see the respective integration.

## Conversations

You can start [Conversations](Conversations.md) with Npc interaction by assigning them in the
[`npc_conversations` section](Conversations.md#binding-conversations-to-npcs) of a quest package.
