package com.tridevmc.architecture.core.math;

public interface ITrans3 {

    IMatrix4 getMatrix();

    IVector3 transform(IVector3 vec);

    IVector3 transformNormal(IVector3 vec);

    IVector3 transformUV(IVector2 vec);



}
