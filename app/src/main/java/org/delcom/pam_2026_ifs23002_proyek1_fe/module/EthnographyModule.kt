package org.delcom.pam_2026_ifs23002_proyek1_fe.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.service.IEthnographyAppContainer
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.service.IEthnographyRepository
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.service.EthnographyAppContainer

@Module
@InstallIn(SingletonComponent::class)
object EthnographyModule {
    @Provides
    fun provideEthnographyContainer(): IEthnographyAppContainer {
        return EthnographyAppContainer()
    }

    @Provides
    fun provideEthnographyRepository(container: IEthnographyAppContainer): IEthnographyRepository {
        return container.repository
    }
}