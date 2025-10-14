## 🌿 Easeon - QuickMend
**Minecraft:** `1.21.10`, `1.21.9`  
**Loader:** `Fabric`  
**Side:** `Server-Side`, `Singleplayer`


## Overview
QuickMend is a server-side mod that allows players to quickly repair items enchanted with Mending by using their experience points.  
When you right-click an anvil while holding a damaged Mending item, it will automatically restore durability based on the XP you have.


## Features
- **Two Repair Modes**: 
  - Normal click: Repairs 10% of max durability
  - Shift + click: Repairs fully using all available XP
- **Experience-Based**: Uses XP at vanilla Mending rate (2 durability per XP)
- **Anvil Durability**: Anvils have 12% chance to degrade (vanilla behavior)


## Commands
All commands require OP level 2 permission (configurable).

**View Current Status:**
```
/easeon quickmend
```
**Enable QuickMend:**
```
/easeon quickmend on
```
**Disable QuickMend:**
```
/easeon quickmend off
```


## Configuration
```json
{
  "enabled": true,
  "requiredOpLevel": 2 // Requires a server restart to take effect.
}
```
`config/easeon/easeon.ss.quickmend.json`
