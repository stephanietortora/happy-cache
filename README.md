# N-Way Set-Associative Cache

happy-cache is a Scala library for in-memory caching.

## Usage 
After cloning the repo, you can generate the archive locally:

```sbtshell
sbt package 
```

This will create the archive in `happy-cache/target/scala-2.12/`

To use in your SBT project, please include the following dependency:

```sbtshell
libraryDependencies += "stephanietortora" % "happy-cache" % "0.1" from "file:///$WORKING_DIR/happy-cache/target/scala-2.12/happy-cache_2.12-0.1.jar"
``` 

Example use of library:  

```scala

import stephanietortora.Cache

object Main extends App {
  val numSet = 3 
  val numEntry = 5 
  val c: Cache[Int, String] = Cache.lruCache(numSet,numEntry)
  val myFunc = (x: Int) => x.toString
  
  val res = c.get(2) match {
  
    // if value is cached, no need to compute 
    case Some(str: String) => str 
   
    // value not cached, compute and cache 
    case None => 
      val str = myFunc(2)
      c.put(2,str)
      str 
    }

}
```