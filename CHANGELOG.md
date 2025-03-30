**v1.6.4**
* Don't force draw the batch if the shader color changes, because that's already accounted for when collecting the data

**v1.6.3**
* Force draw batch if the scissor box or shader color changes (Fixes issue with Figura player list rendering)
* Clamp out of bounds shader color (Fixes issue with Dragon Survival "Magic Cast Meter")
* Disable experimental sign buffering if Effective is installed (Effective uses non-standard rendering which isn't compatible)
