camera {
	location < 0.0,  0.0,  1.5 >
	 up < 0.0,  1.0,  0.0 >
	 right < -1.3300000429153442,  0.0,  0.0 >
	 look_at < 0.0,  0.0,  0.0 >
	angle 60.0
}
background {
	color rgb < 1.0,  1.0,  1.0 >
}
light_source {
	  < 2.0,  1.0,  1.5 >
	color rgb < 1.0,  1.0,  1.0 >
}
light_source {
	  < 2.0,  1.0,  1.5 >
	color rgb < 1.0,  1.0,  1.0 >
}
union {
#declare crossPlane= <1,1,1>;
#declare visibleFinish = finish { reflection 0.3 phong 1 };
#declare invisibleFinish = finish { phong 0 };
#declare GDyingA = color rgb < 0.0 , 0.0 , 0.0 >;
#declare GDyingAFinish = visibleFinish;
#declare Acidogen = color rgb < 0.0 , 1.0 , 0.0 >;
#declare AcidogenFinish = visibleFinish;
#declare Methanogen = color rgb < 1.0 , 0.0 , 0.0 >;
#declare MethanogenFinish = visibleFinish;
#declare GDyingM = color rgb < 0.0 , 0.0 , 0.0 >;
#declare GDyingMFinish = visibleFinish;
#macro FinishMacro(loc, fin, col)
  #if(loc.x<=crossPlane.x & loc.y<=crossPlane.y & loc.z<=crossPlane.z)
	  pigment { col }
	  finish{ fin }
  #else
	  pigment { rgbt<1,1,1,1> }
	  finish { invisibleFinish }
  #end
#end