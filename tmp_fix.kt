         } catch (e: Exception) {
             e.printStackTrace()
             throw e // Re-throw the exception so the caller knows resolution failed
         }
     }