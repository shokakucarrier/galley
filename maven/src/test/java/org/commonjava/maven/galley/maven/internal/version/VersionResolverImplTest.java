package org.commonjava.maven.galley.maven.internal.version;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.testutil.TestFixture;
import org.commonjava.maven.galley.maven.version.LatestVersionSelectionStrategy;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.testing.core.transport.job.TestDownload;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class VersionResolverImplTest
{

    private static final Location LOCATION = new SimpleLocation( "test:version-resolver/1" );

    private static final Location LOCATION2 = new SimpleLocation( "test:version-resolver/2" );

    private static final List<? extends Location> ONE_LOCATION = Collections.singletonList( LOCATION );

    private static final List<? extends Location> TWO_LOCATIONS = Arrays.asList( LOCATION, LOCATION2 );

    private static final String ROOT = "version-resolver/";

    @Rule
    public TestFixture fixture = new TestFixture();

    @Before
    public void before()
    {
        fixture.initMissingComponents();
    }

    @Test
    public void resolveSnapshot_FirstMatch_SingletonLocationList_SingletonSnapshotList_LatestVersionStrategy()
        throws Exception
    {
        final String testResource = "single-snapshot/single-snapshot.xml";

        final ProjectVersionRef ref = new ProjectVersionRef( "org.group", "artifact", "1.0-SNAPSHOT" );
        final ConcreteResource cr = new ConcreteResource( LOCATION, fixture.snapshotMetadataPath( ref ) );
        final TestDownload download = new TestDownload( ROOT + testResource );

        fixture.getTransport()
               .registerDownload( cr, download );

        final ProjectVersionRef result =
            fixture.getVersionResolver()
                   .resolveFirstMatchVariableVersion( ONE_LOCATION, ref, LatestVersionSelectionStrategy.INSTANCE );

        assertThat( result, notNullValue() );
        assertThat( result.getVersionString(), equalTo( "1.0-20140604.101244-1" ) );
    }

    @Test
    public void resolveSnapshot_FirstMatch_SingletonLocationList_TwoSnapshotList_LatestVersionStrategy()
        throws Exception
    {
        final String testResource = "2-snapshots-1-location/two-snapshots.xml";

        final ProjectVersionRef ref = new ProjectVersionRef( "org.group2", "artifact", "1.0-SNAPSHOT" );
        final ConcreteResource cr = new ConcreteResource( LOCATION, fixture.snapshotMetadataPath( ref ) );
        final TestDownload download = new TestDownload( ROOT + testResource );

        fixture.getTransport()
               .registerDownload( cr, download );

        final ProjectVersionRef result =
            fixture.getVersionResolver()
                   .resolveFirstMatchVariableVersion( ONE_LOCATION, ref, LatestVersionSelectionStrategy.INSTANCE );

        assertThat( result, notNullValue() );
        assertThat( result.getVersionString(), equalTo( "1.0-20140604.102909-1" ) );
    }

    @Test
    public void resolveSnapshot_FirstMatch_TwoLocationList_TwoSingletonSnapshotList_LatestVersionStrategy()
        throws Exception
    {
        final String testResource = "2-snapshots-2-locations/maven-metadata-1.xml";
        final String testResource2 = "2-snapshots-2-locations/maven-metadata-2.xml";

        final ProjectVersionRef ref = new ProjectVersionRef( "org.group2", "artifact", "1.0-SNAPSHOT" );

        final String path = fixture.snapshotMetadataPath( ref );

        fixture.getTransport()
               .registerDownload( new ConcreteResource( LOCATION, path ), new TestDownload( ROOT + testResource ) );

        fixture.getTransport()
               .registerDownload( new ConcreteResource( LOCATION2, path ), new TestDownload( ROOT + testResource2 ) );

        final ProjectVersionRef result =
            fixture.getVersionResolver()
                   .resolveFirstMatchVariableVersion( TWO_LOCATIONS, ref, LatestVersionSelectionStrategy.INSTANCE );

        assertThat( result, notNullValue() );

        // newest snapshot is in the SECOND location, but we're using first-match semantics here.
        assertThat( result.getVersionString(), equalTo( "1.0-20140604.101244-1" ) );
    }

    @Test
    public void resolveSnapshot_Latest_TwoLocationList_TwoSingletonSnapshotList_LatestVersionStrategy()
        throws Exception
    {
        final String testResource = "2-snapshots-2-locations/maven-metadata-1.xml";
        final String testResource2 = "2-snapshots-2-locations/maven-metadata-2.xml";

        final ProjectVersionRef ref = new ProjectVersionRef( "org.group2", "artifact", "1.0-SNAPSHOT" );

        final String path = fixture.snapshotMetadataPath( ref );

        fixture.getTransport()
               .registerDownload( new ConcreteResource( LOCATION, path ), new TestDownload( ROOT + testResource ) );

        fixture.getTransport()
               .registerDownload( new ConcreteResource( LOCATION2, path ), new TestDownload( ROOT + testResource2 ) );

        final ProjectVersionRef result =
            fixture.getVersionResolver()
                   .resolveLatestVariableVersion( TWO_LOCATIONS, ref, LatestVersionSelectionStrategy.INSTANCE );

        assertThat( result, notNullValue() );

        // newest snapshot is in the SECOND location.
        assertThat( result.getVersionString(), equalTo( "1.0-20140604.102909-1" ) );
    }

}